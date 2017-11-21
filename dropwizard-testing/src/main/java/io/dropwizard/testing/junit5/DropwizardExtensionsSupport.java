package io.dropwizard.testing.junit5;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.ReflectionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * An JUnit 5 extension that looks for fields in test class and test class instances and executes before and after actions.
 */
public class DropwizardExtensionsSupport implements BeforeAllCallback, BeforeEachCallback, AfterAllCallback, AfterEachCallback {

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
            for (Field member : findAnnotatedFields(extensionContext.getRequiredTestClass(), true)) {
                ((DropwizardExtension) ReflectionUtils.makeAccessible(member).get(null)).after();
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {
        final Object testInstance = extensionContext.getTestInstance()
            .orElseThrow(() -> new IllegalStateException("Unable to get the current test instance"));
        try {
            for (Field member : findAnnotatedFields(testInstance.getClass(), false)) {
                ((DropwizardExtension) ReflectionUtils.makeAccessible(member).get(testInstance)).after();
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        try {
            for (Field member : findAnnotatedFields(extensionContext.getRequiredTestClass(), true)) {
                ((DropwizardExtension) ReflectionUtils.makeAccessible(member).get(null)).before();
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        final Object testInstance = extensionContext.getTestInstance()
            .orElseThrow(() -> new IllegalStateException("Unable to get the current test instance"));
        try {
            for (Field member : findAnnotatedFields(testInstance.getClass(), false)) {
                ((DropwizardExtension) ReflectionUtils.makeAccessible(member).get(testInstance)).before();
            }
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }
}
