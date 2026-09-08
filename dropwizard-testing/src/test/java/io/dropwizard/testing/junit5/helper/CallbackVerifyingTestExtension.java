package io.dropwizard.testing.junit5.helper;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

// Runs delayed assertions after other extensions' afterEach()/afterAll() calls, so we can observe both before() and
// after() side effects (e.g. DropwizardExtensionsSupport invoking after() on counted extensions) from within the same
// test lifecycle.
//
// This extension must be REGISTERED FIRST (before every other @ExtendWith) so that its afterEach/afterAll run LAST;
// JUnit fires AfterEachCallback and AfterAllCallback in REVERSE registration order.
//
// Two delayed-assertion lists are supported:
//   - getDelayedAssertions()           - runs at afterEach (per-test-method scope).
//   - getClassLevelDelayedAssertions() - runs at afterAll (per-class scope).
public class CallbackVerifyingTestExtension implements BeforeEachCallback, AfterEachCallback, AfterAllCallback {
    private static final String STATIC_ASSERTIONS_METHOD = "staticClassLevelDelayedAssertions";

    @FunctionalInterface
    public interface Invokable {
        void invoke();
    }

    // Tests that need to observe state *after* DropwizardExtensionsSupport's callbacks implement this and add
    // assertions to the returned lists; CallbackVerifyingTestExtension invokes them at the appropriate lifecycle
    // moment. The class-level list is only useful under @TestInstance(PER_CLASS).
    public interface DelayedAssertionsTest {
        List<Invokable> getDelayedAssertions();

        default List<Invokable> getClassLevelDelayedAssertions() {
            return Collections.emptyList();
        }
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        verifyRegisteredFirst(context.getRequiredTestClass());
    }

    @Override
    public void afterEach(ExtensionContext context) {
        DelayedAssertionsTest testInstance = (DelayedAssertionsTest) context.getTestInstance()
            .orElseThrow(() -> new AssertionError("Null context.testInstance"));
        testInstance.getDelayedAssertions()
            .forEach(Invokable::invoke);
    }

    @Override
    public void afterAll(ExtensionContext context) {
        // Instance-based path (PER_CLASS test classes implementing DelayedAssertionsTest).
        context.getTestInstance().ifPresent(instance -> {
            DelayedAssertionsTest testInstance = (DelayedAssertionsTest) instance;
            testInstance.getClassLevelDelayedAssertions()
                .forEach(Invokable::invoke);
        });

        // Static-holder path (any test class - useful under PER_METHOD where no instance exists at afterAll).
        invokeStaticClassLevelAssertions(context.getRequiredTestClass());
    }

    // Reflectively looks for a public no-arg static method named 'staticClassLevelDelayedAssertions' returning
    // List<Invokable> on the given class. If present, invokes it and runs each returned Invokable.
    //
    // Rationale: Under PER_METHOD, no persistent test instance exists at afterAll, so the instance-based
    // getClassLevelDelayedAssertions() path is unreachable.
    @SuppressWarnings("unchecked")
    private static void invokeStaticClassLevelAssertions(Class<?> testClass) {
        Method method;
        try {
            method = testClass.getMethod(STATIC_ASSERTIONS_METHOD);
        } catch (NoSuchMethodException e) {
            // Opt-in: absent method means no static-holder assertions to run.
            return;
        }

        if (!Modifier.isStatic(method.getModifiers())) {
            throw new IllegalStateException(
                testClass.getName() + "." + STATIC_ASSERTIONS_METHOD + "() must be static");
        }
        if (!List.class.isAssignableFrom(method.getReturnType())) {
            throw new IllegalStateException(
                testClass.getName() + "." + STATIC_ASSERTIONS_METHOD + "() must return List<Invokable>");
        }

        List<Invokable> assertions;
        try {
            method.setAccessible(true);
            assertions = (List<Invokable>) method.invoke(null);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new IllegalStateException(
                "Failed to invoke " + testClass.getName() + "." + STATIC_ASSERTIONS_METHOD + "()", e);
        }

        if (assertions == null) {
            return;
        }
        assertions.forEach(Invokable::invoke);
    }

    // Concatenate @ExtendWith values across the enclosing-class chain in registration order (outermost first, then
    // inward). At each enclosing level, also walk the superclass chain (superclass -> self) to mirror JUnit's own
    // @ExtendWith registration order (see JUnit's ExtendWith javadoc: extensions from superclasses register before
    // subclass extensions). Verify that CallbackVerifyingTestExtension is the first entry.
    private static void verifyRegisteredFirst(Class<?> testClass) {
        List<Class<?>> chainOutermostFirst = enclosingChainOutermostFirst(testClass);

        for (Class<?> c : chainOutermostFirst) {
            List<Class<? extends Extension>> extensions = collectExtensionsWithInheritance(c);
            if (extensions.isEmpty()) {
                continue;
            }
            Class<? extends Extension> firstOnThisClass = extensions.get(0);
            if (firstOnThisClass != CallbackVerifyingTestExtension.class) {
                throw new IllegalStateException("CallbackVerifyingTestExtension must be declared first among "
                    + "@ExtendWith annotations. Problematic test class: " + testClass.getName());
            }
            return;
        }

        // No class in the enclosing chain has any @ExtendWith at all - impossible in practice
        throw new IllegalStateException("CallbackVerifyingTestExtension.beforeEach fired but no @ExtendWith was found "
            + "in the enclosing class chain of " + testClass.getName());
    }

    private static List<Class<?>> enclosingChainOutermostFirst(Class<?> testClass) {
        List<Class<?>> chain = new ArrayList<>();
        for (Class<?> c = testClass; c != null; c = c.getEnclosingClass()) {
            chain.add(c);
        }
        Collections.reverse(chain);
        return chain;
    }

    // Walks the superclass chain from the topmost non-Object ancestor down to the class itself, collecting
    // @ExtendWith values in registration order at each level.
    //
    // We cannot use getAnnotationsByType() to rely on @Inherited semantics - JDK's @Inherited + @Repeatable is
    // broken in the redeclaration case (a subclass @ExtendWith masks the parent's entirely).
    private static List<Class<? extends Extension>> collectExtensionsWithInheritance(Class<?> c) {
        List<Class<?>> superChain = new ArrayList<>();
        for (Class<?> s = c; s != null && s != Object.class; s = s.getSuperclass()) {
            superChain.add(s);
        }
        Collections.reverse(superChain);
        return superChain.stream()
            .flatMap(cls -> extractExtendWithClasses(cls).stream())
            .toList();
    }

    private static List<Class<? extends Extension>> extractExtendWithClasses(Class<?> c) {
        return Arrays.stream(c.getDeclaredAnnotationsByType(ExtendWith.class))
            .flatMap(annotation -> Arrays.stream(annotation.value()))
            .toList();
    }
}
