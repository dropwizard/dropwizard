package io.dropwizard.testing.junit5;

import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.util.ReflectionUtils;

import javax.annotation.Nullable;

import io.dropwizard.testing.DropwizardTestSupport;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An JUnit 5 extension that looks for fields in test class and test class instances and executes before and after actions.
 */
public class DropwizardExtensionsSupport implements BeforeAllCallback, BeforeEachCallback, AfterAllCallback, AfterEachCallback, TestExecutionExceptionHandler {

    private static Set<Field> findAnnotatedFields(Class<?> testClass, boolean isStaticMember) {
        final Set<Field> set = Arrays.stream(testClass.getDeclaredFields()).
                filter(m -> isStaticMember == Modifier.isStatic(m.getModifiers())).
                filter(m -> DropwizardExtension.class.isAssignableFrom(m.getType())).
                collect(Collectors.toSet());
        if (!testClass.getSuperclass().equals(Object.class)) {
            set.addAll(findAnnotatedFields(testClass.getSuperclass(), isStaticMember));
        }
        return set;
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        try {
            afterAll(extensionContext.getRequiredTestClass());
        } catch (Exception e) {
            throw e;
        } catch (Throwable e) {
            throw new Exception(e);
        }
    }

    private void afterAll(Class<?> cls) throws Throwable {
        final Class<?> enclosingClass = cls.getEnclosingClass();
        if (enclosingClass != null) {
            afterAll(enclosingClass);
        }
        for (Field member : findAnnotatedFields(cls, true)) {
            getDropwizardExtension(member, null).after();
        }

        handleTestExecutionException( null, cls, null);
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {
        final Object testInstance = extensionContext.getTestInstance()
                .orElseThrow(() -> new IllegalStateException("Unable to get the current test instance"));
        try {
            afterEach(testInstance, testInstance.getClass());
        } catch (Exception e) {
            throw e;
        } catch (Throwable e) {
            throw new Exception(e);
        }
    }

    private void afterEach(Object testInstance, Class<?> cls) throws Throwable {
        final Class<?> enclosingClass = testInstance.getClass().getEnclosingClass();
        if (enclosingClass != null) {
            final Object enclosing = getEnclosingInstance(testInstance);
            if (enclosing != null) {
                afterEach(enclosing, enclosingClass);
            }
        }
        for (Field member : findAnnotatedFields(cls, false)) {
            getDropwizardExtension(member, testInstance).after();
        }

        handleTestExecutionException(testInstance, cls, null);
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        try {
            beforeAll(extensionContext.getRequiredTestClass());
        } catch (Exception e) {
            throw e;
        } catch (Throwable e) {
            throw new Exception(e);
        }
    }

    private void beforeAll(Class<?> cls) throws Throwable {
        final Class<?> enclosingClass = cls.getEnclosingClass();
        if (enclosingClass != null) {
            beforeAll(enclosingClass);
        }
        for (Field member : findAnnotatedFields(cls, true)) {
            getDropwizardExtension(member, null).before();
        }
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        final Object testInstance = extensionContext.getTestInstance()
                .orElseThrow(() -> new IllegalStateException("Unable to get the current test instance"));
        try {
            beforeEach(testInstance, testInstance.getClass());
        } catch (Exception e) {
            throw e;
        } catch (Throwable e) {
            throw new Exception(e);
        }
    }

    private void beforeEach(Object testInstance, Class<?> cls) throws Throwable {
        final Class<?> enclosingClass = cls.getEnclosingClass();
        if (enclosingClass != null) {
            final Object enclosing = getEnclosingInstance(testInstance);
            if (enclosing != null) {
                beforeEach(enclosing, cls);
            }
        }
        for (Field member : findAnnotatedFields(testInstance.getClass(), false)) {
            getDropwizardExtension(member, testInstance).before();
        }
    }

    @Nullable
    private Object getEnclosingInstance(Object o) throws IllegalAccessException {
        final Class<?> innerClass = o.getClass();
        if (innerClass.getEnclosingClass() == null) {
            return null;
        }

        // https://stackoverflow.com/a/15265900
        // 10 levels of nested classes should be enough for everyone
        for (int i = 0; i < 10; i++) {
            try {
                final Field field = innerClass.getDeclaredField("this$" + i);
                field.setAccessible(true);
                return field.get(o);
            } catch (NoSuchFieldException e) {
                // NOP
            }
        }
        return null;
    }

    private DropwizardExtension getDropwizardExtension(Field member, @Nullable Object o) throws IllegalAccessException {
        return (DropwizardExtension) ReflectionUtils.makeAccessible(member).get(o);
    }

    @Override
    public void handleTestExecutionException(ExtensionContext extensionContext, Throwable throwable) throws Throwable
    {
        final Object testInstance = extensionContext.getTestInstance()
            .orElseThrow(() -> new IllegalStateException("Unable to get the current test instance"));
        try {
            handleTestExecutionException(testInstance, testInstance.getClass(), throwable);
        } catch (Exception e) {
            throw e;
        } catch (Throwable e) {
            throw new Exception(e);
        }

        throw throwable;
    }

    /**
     * Invokes {@link DropwizardAppExtension#scrapeServerErrors()} after each test to prevent exceptions from leaking between tests
     */
    private void handleTestExecutionException(@Nullable Object testInstance, Class<? extends Object> cls,@Nullable Throwable throwable) throws Throwable
    {
        if(testInstance != null) {
            final Class<?> enclosingClass = testInstance.getClass().getEnclosingClass();
            if (enclosingClass != null) {
                final Object enclosing = getEnclosingInstance(testInstance);
                if (enclosing != null) {
                    handleTestExecutionException(enclosing, enclosingClass, throwable);
                }
            }

            for (Field member : findAnnotatedFields(cls, false)) {
                attachServerErrors(getDropwizardExtension(member, testInstance), throwable);
            }
        }

        for (Field member : findAnnotatedFields(cls, true)) {
            attachServerErrors(getDropwizardExtension(member, null), throwable);
        }
    }

    private void attachServerErrors(DropwizardExtension dropwizardExtension, @Nullable Throwable throwable)
    {
       if(dropwizardExtension instanceof DropwizardAppExtension) {
           List<Throwable> serverSideErrors = ((DropwizardAppExtension)dropwizardExtension).scrapeServerErrors();
           if(throwable != null) {
               serverSideErrors.forEach(throwable::addSuppressed);
           }
       }
    }
}
