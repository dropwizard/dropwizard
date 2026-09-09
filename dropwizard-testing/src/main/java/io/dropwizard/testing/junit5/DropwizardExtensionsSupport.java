package io.dropwizard.testing.junit5;

import com.google.common.base.Throwables;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.platform.commons.support.ReflectionSupport;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * A JUnit 5 extension that looks for {@link DropwizardExtension} fields in test class instances and drives their
 * {@link DropwizardExtension#before()} / {@link DropwizardExtension#after()} at the appropriate lifecycle moments.
 * <p>
 * Field-lifecycle mapping:
 * <ul>
 *     <li>Static fields: fire at {@code beforeAll} / {@code afterAll} (class-scoped).</li>
 *     <li>Non-static fields under {@link TestInstance.Lifecycle#PER_METHOD} (default): fire at
 *         {@code beforeEach} / {@code afterEach} (per test method).</li>
 *     <li>Non-static fields under {@link TestInstance.Lifecycle#PER_CLASS}: fire at
 *         {@code beforeAll} / {@code afterAll} (class-scoped, matching JUnit's own
 *         {@code @RegisterExtension} semantics).</li>
 * </ul>
 * <p>
 * Field ordering: fields are iterated in declaration order (from {@link Class#getDeclaredFields()} and superclass
 * chain, outermost-first). {@link Order @Order} annotations on fields override declaration order. {@code before()}
 * calls happen in ascending order; {@code after()} calls happen in reverse for setup/teardown symmetry.
 * <p>
 * Failure handling:
 * <ul>
 *     <li>If {@code before()} throws mid-iteration, {@code after()} is called on already-succeeded fields in
 *         reverse (rollback), and the original exception is rethrown with any rollback failures attached as
 *         suppressed exceptions.</li>
 *     <li>If {@code after()} throws mid-iteration, iteration continues on remaining fields (best-effort cleanup),
 *         and the first exception is rethrown with any subsequent failures attached as suppressed exceptions.</li>
 * </ul>
 */
public class DropwizardExtensionsSupport
    implements BeforeAllCallback, BeforeEachCallback, AfterAllCallback, AfterEachCallback {

    private record InstanceAndField(@Nullable Object instance, Field field) { }

    // utility methods for interacting with ExtensionContext.Namespace for this extension
    private static final class ExtensionContextUtil {
        private static final Namespace NAMESPACE = Namespace.create(DropwizardExtensionsSupport.class);

        // Keys within NAMESPACE:
        //   String SUCCEEDED_BEFORE_INSTANCES_KEY -> List<InstanceAndField>  (method-scoped: context.getStore)
        //   Class<?> testClass                    -> Boolean isPerClass      (root-scoped: context.getRoot().getStore)
        private static final String SUCCEEDED_BEFORE_INSTANCES_KEY = "succeededBeforeInstances";

        static void putBeforeInstanceList(ExtensionContext context, List<InstanceAndField> list) {
            context.getStore(NAMESPACE)
                .put(SUCCEEDED_BEFORE_INSTANCES_KEY, list);
        }

        @Nullable
        @SuppressWarnings("unchecked")
        static List<InstanceAndField> getBeforeInstancesList(ExtensionContext context) {
            return (List<InstanceAndField>) context.getStore(NAMESPACE)
                .get(SUCCEEDED_BEFORE_INSTANCES_KEY, List.class);
        }

        static void putClassLifecycle(ExtensionContext context, Class<?> classKey, boolean isPerClass) {
            context.getRoot().getStore(NAMESPACE)
                .put(classKey, isPerClass);
        }

        @Nullable
        static Boolean getClassLifecycle(ExtensionContext context, Class<?> classKey) {
            return context.getRoot().getStore(NAMESPACE)
                .get(classKey, Boolean.class);
        }

        private ExtensionContextUtil() {
            // prevent construction
        }
    }

    @Override
    public void beforeAll(ExtensionContext context) throws Exception {
        // Record the lifecycle for this class so beforeEach's enclosing-instance walk can look it up consistently.
        // We learn whether PER_CLASS lifecycle applies from JUnit since its rules are more complex than just
        // inspecting attached annotations via reflection.
        ExtensionContextUtil.putClassLifecycle(context, context.getRequiredTestClass(), isPerClass(context));

        // Static fields always fire at beforeAll (their natural class-level scope).
        invokeBeforeAll(
            collectStaticFields(context.getRequiredTestClass()),
            null);  // testInstance=null
        // Non-static fields fire only under PER_CLASS.
        if (isPerClass(context)) {
            Object testInstance = context.getRequiredTestInstance();
            invokeBeforeAll(collectInstanceFields(testInstance.getClass()), testInstance);
        }
    }

    @Override
    public void afterAll(ExtensionContext context) throws Exception {
        // Assemble the full teardown list in the order after() calls should fire: PER_CLASS instance fields first
        // (reversed for setup/teardown symmetry), then static fields (also reversed). Building one list lets us share
        // invocation + error-aggregation logic with afterEach.
        List<InstanceAndField> teardown = new ArrayList<>();
        if (isPerClass(context)) {
            Object testInstance = context.getRequiredTestInstance();
            for (Field f : copyAndReverse(collectInstanceFields(testInstance.getClass()))) {
                teardown.add(new InstanceAndField(testInstance, f));
            }
        }
        for (Field f : copyAndReverse(collectStaticFields(context.getRequiredTestClass()))) {
            teardown.add(new InstanceAndField(null, f));
        }
        invokeAfter(teardown);
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        // Non-static fields fire here only under PER_METHOD. Also walk enclosing classes so nested tests can
        // reach outer-class non-static fields (see #2906, #4205).
        if (isPerClass(context)) {
            return;
        }
        Object testInstance = context.getRequiredTestInstance();
        List<InstanceAndField> successfulBeforeInstances = invokeBeforeEachWithEnclosingWalk(testInstance, context);
        // Store the successfully-invoked set so afterEach knows which fields to clean up. JUnit fires afterEach
        // unconditionally even when beforeEach threw, so afterEach must not touch fields whose before() never ran.
        ExtensionContextUtil.putBeforeInstanceList(context, successfulBeforeInstances);
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        if (isPerClass(context)) {
            return;
        }
        final List<InstanceAndField> successfulBeforeInstances = ExtensionContextUtil.getBeforeInstancesList(context);
        if (successfulBeforeInstances == null) {
            // beforeEach didn't run or threw before storing - nothing to clean up.
            return;
        }
        // Reverse: last-success is cleaned up first.
        invokeAfter(copyAndReverse(successfulBeforeInstances));
    }

    // ---------------------------------------------------------------------------------------------------
    // Invocation helpers.
    //
    // invokeBeforeEachWithEnclosingWalk: walks the enclosing-instance chain because JUnit fires beforeEach only at
    // the innermost test context; outer classes' non-static fields would otherwise be missed on nested tests.
    //
    // invokeBeforeAll: no walk - JUnit fires beforeAll separately for each class-level in a nested-class tree, so
    // walking would double-fire.
    //
    // invokeAfter: shared teardown used by both afterAll and afterEach. Iterates the caller-supplied list and
    // aggregates any thrown exceptions (best-effort cleanup).
    // ---------------------------------------------------------------------------------------------------

    private List<InstanceAndField> invokeBeforeEachWithEnclosingWalk(Object testInstance, ExtensionContext context)
        throws Exception {
        List<Object> instancesOutermostFirst = enclosingInstanceChainOutermostFirst(testInstance);
        List<InstanceAndField> successfulBeforeInstances = new ArrayList<>();
        for (Object instance : instancesOutermostFirst) {
            // Skip instances whose class is PER_CLASS - their non-static fields were already fired at that class's
            // beforeAll and would be double-fired here.
            if (isPerClass(instance.getClass(), context)) {
                continue;
            }
            List<Field> fields = collectInstanceFields(instance.getClass());
            for (Field field : fields) {
                try {
                    getDropwizardExtension(field, instance).before();
                    successfulBeforeInstances.add(new InstanceAndField(instance, field));
                } catch (Throwable primary) {
                    // Rollback: after() on already-successful fields, in reverse. We throw before returning, so the
                    // caller never stores this list - afterEach sees no entry and skips (no risk of double-cleanup).
                    for (int i = successfulBeforeInstances.size() - 1; i >= 0; i--) {
                        InstanceAndField iaf = successfulBeforeInstances.get(i);
                        try {
                            getDropwizardExtension(iaf.field, iaf.instance).after();
                        } catch (Throwable rollback) {
                            primary.addSuppressed(rollback);
                        }
                    }

                    Throwables.throwIfInstanceOf(primary, Exception.class);
                    Throwables.throwIfUnchecked(primary);
                    throw new Exception(primary);
                }
            }
        }
        return successfulBeforeInstances;
    }

    private void invokeBeforeAll(List<Field> fields, @Nullable Object testInstance) throws Exception {
        List<Field> successfulFields = new ArrayList<>();
        for (Field field : fields) {
            try {
                getDropwizardExtension(field, testInstance).before();
                successfulFields.add(field);
            } catch (Throwable primary) {
                for (int i = successfulFields.size() - 1; i >= 0; i--) {
                    try {
                        getDropwizardExtension(successfulFields.get(i), testInstance).after();
                    } catch (Throwable rollback) {
                        primary.addSuppressed(rollback);
                    }
                }

                Throwables.throwIfInstanceOf(primary, Exception.class);
                Throwables.throwIfUnchecked(primary);
                throw new Exception(primary);
            }
        }
    }

    // Best-effort teardown: invoke after() on every pair in list order, aggregating any exceptions via addSuppressed.
    // Iteration order is the caller's responsibility - callers hand us pairs in reverse of setup order for
    // setup/teardown symmetry.
    private void invokeAfter(List<InstanceAndField> successfulBeforeInstances) throws Exception {
        Throwable thrown = null;
        for (InstanceAndField iaf : successfulBeforeInstances) {
            try {
                getDropwizardExtension(iaf.field(), iaf.instance()).after();
            } catch (Throwable t) {
                thrown = updateOrAddSuppressed(thrown, t);
            }
        }
        if (thrown != null) {
            Throwables.throwIfInstanceOf(thrown, Exception.class);
            Throwables.throwIfUnchecked(thrown);
            throw new Exception(thrown);
        }
    }

    // ---------------------------------------------------------------------------------------------------
    // Field discovery.
    // ---------------------------------------------------------------------------------------------------

    // Static @DropwizardExtension fields declared on the given class or any superclass, in declaration order,
    // sorted stably by @Order annotation.
    private static List<Field> collectStaticFields(Class<?> testClass) {
        return collectFields(
            testClass,
            true);      // wantStatic=true
    }

    // Non-static @DropwizardExtension fields declared on the given class or any superclass, in declaration order,
    // sorted stably by @Order annotation.
    private static List<Field> collectInstanceFields(Class<?> testClass) {
        return collectFields(
            testClass,
            false);     // wantStatic=false
    }

    private static List<Field> collectFields(Class<?> testClass, boolean wantStatic) {
        // Walk superclass chain, outermost superclass first so parent fields are registered before subclass fields
        // (mirrors JUnit's own @ExtendWith ordering).
        List<Class<?>> classChain = new ArrayList<>();
        for (Class<?> c = testClass; c != null && !c.equals(Object.class); c = c.getSuperclass()) {
            classChain.add(c);
        }
        Collections.reverse(classChain);

        List<Field> fields = new ArrayList<>();
        for (Class<?> c : classChain) {
            for (Field field : c.getDeclaredFields()) {
                if (wantStatic != Modifier.isStatic(field.getModifiers())) {
                    continue;
                }
                if (!DropwizardExtension.class.isAssignableFrom(field.getType())) {
                    continue;
                }
                fields.add(field);
            }
        }
        // Stable sort by @Order (default order matches JUnit's Order.DEFAULT = Integer.MAX_VALUE / 2).
        fields.sort(Comparator.comparingInt(DropwizardExtensionsSupport::orderOf));
        return fields;
    }

    private static int orderOf(Field field) {
        Order order = field.getAnnotation(Order.class);
        return order != null ? order.value() : Order.DEFAULT;
    }

    // Returns the enclosing-instance chain starting from the outermost enclosing instance, ending with the
    // provided testInstance (innermost). Never null; always contains at least testInstance.
    private static List<Object> enclosingInstanceChainOutermostFirst(Object testInstance) {
        List<Object> chain = new ArrayList<>();
        Object current = testInstance;
        while (current != null) {
            chain.add(current);
            current = getEnclosingInstance(current);
        }
        Collections.reverse(chain);
        return chain;
    }

    @Nullable
    private static Object getEnclosingInstance(Object o) {
        Class<?> innerClass = o.getClass();
        if (innerClass.getEnclosingClass() == null) {
            return null;
        }
        // javac emits a synthetic field named this$N holding the enclosing-instance reference. The N reflects the
        // class's nesting depth for name-mangling; only one such field exists per class. If none exists (e.g. static
        // nested), the chain ends here.
        Field enclosingRef = Arrays.stream(innerClass.getDeclaredFields())
            .filter(f -> f.isSynthetic() && f.getName().startsWith("this$"))
            .findFirst()
            .orElse(null);
        if (enclosingRef == null) {
            return null;
        }
        return ReflectionSupport.tryToReadFieldValue(enclosingRef, o)
            .getOrThrow(e -> new IllegalStateException(
                "Failed to read enclosing-instance field " + enclosingRef, e));
    }

    private static DropwizardExtension getDropwizardExtension(Field member, @Nullable Object o) {
        return ReflectionSupport.tryToReadFieldValue(member, o)
            .andThenTry(DropwizardExtension.class::cast)
            .getOrThrow(e -> new IllegalStateException(
                "Failed to read " + DropwizardExtension.class.getSimpleName() + " field: " + member, e));
    }

    // ---------------------------------------------------------------------------------------------------
    // Miscellaneous helpers.
    // ---------------------------------------------------------------------------------------------------

    private static boolean isPerClass(ExtensionContext context) {
        return context.getTestInstanceLifecycle()
            .filter(l -> l == TestInstance.Lifecycle.PER_CLASS)
            .isPresent();
    }

    // Whether the given class was recorded as PER_CLASS during its beforeAll. If no entry exists, beforeAll never
    // fired for this class - which means no non-static fields were fired at beforeAll and therefore the class cannot
    // be participating in PER_CLASS field lifecycle.
    private static boolean isPerClass(Class<?> clazz, ExtensionContext context) {
        return Boolean.TRUE.equals(ExtensionContextUtil.getClassLifecycle(context, clazz));
    }

    private static <T> List<T> copyAndReverse(List<T> in) {
        List<T> out = new ArrayList<>(in);
        Collections.reverse(out);
        return out;
    }

    private static <T extends Throwable> T updateOrAddSuppressed(@Nullable T lastException, T newException) {
        if (lastException == null) {
            return newException;
        } else {
            lastException.addSuppressed(newException);
            return lastException;
        }
    }
}
