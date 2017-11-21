package io.dropwizard.testing.junit5;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * An JUnit 5 extension that looks for fields in test class and test class instances and executes before and after actions.
 */
public class DropwizardExtensionsSupport implements BeforeAllCallback, BeforeEachCallback, AfterAllCallback, AfterEachCallback {
    private List<Field> findAnnotatedFields(Class<?> testClass, boolean staticMember) {
        List<Field> list = Arrays.stream(testClass.getDeclaredFields()).
            filter(m -> staticMember == Modifier.isStatic(m.getModifiers())).
            filter(m -> DropwizardExtension.class.isAssignableFrom(m.getType())).
            distinct().
            collect(Collectors.toList());

        if (!testClass.getSuperclass().equals(Object.class)) {
            list.addAll(findAnnotatedFields(testClass.getSuperclass(), staticMember));
        }

        return list;
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        List<Field> members = findAnnotatedFields(extensionContext.getRequiredTestClass(), true);

        members.stream().
            forEach(member -> {
                try {
                    ((DropwizardExtension) ReflectionUtils.makeAccessible(member).get(null)).
                        after();
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            });
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {
        Object testInstance = extensionContext.getTestInstance().get();

        List<Field> members = findAnnotatedFields(testInstance.getClass(), false);

        members.stream().
            forEach(member -> {
                try {
                    ((DropwizardExtension) ReflectionUtils.makeAccessible(member).get(testInstance)).
                        after();
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            });
    }

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        List<Field> members = findAnnotatedFields(extensionContext.getRequiredTestClass(), true);

        members.stream().
            forEach(member -> {
                try {
                    ((DropwizardExtension) ReflectionUtils.makeAccessible(member).get(null)).
                        before();
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            });
    }

    @Override
    public void beforeEach(ExtensionContext extensionContext) throws Exception {
        Object testInstance = extensionContext.getTestInstance().get();

        List<Field> members = findAnnotatedFields(testInstance.getClass(), false);

        members.stream().
            forEach(member -> {
                try {
                    ((DropwizardExtension) ReflectionUtils.makeAccessible(member).get(testInstance)).
                        before();
                } catch (Throwable e) {
                    throw new RuntimeException(e);
                }
            });
    }
}
