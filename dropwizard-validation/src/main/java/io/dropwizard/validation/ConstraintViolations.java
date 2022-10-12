package io.dropwizard.validation;

import jakarta.validation.ConstraintViolation;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * This class provides static methods to work with constraint violations.
 */
public class ConstraintViolations {
    private ConstraintViolations() { /* singleton */ }

    /**
     * Computes a string representation from a {@link ConstraintViolation}.
     *
     * @param v the constraint violation
     * @return the formatted message
     * @param <T> the generic type of the constraint violation
     */
    public static <T> String format(ConstraintViolation<T> v) {
        if (v.getConstraintDescriptor().getAnnotation() instanceof ValidationMethod) {
            return v.getMessage();
        } else {
            return String.format("%s %s", v.getPropertyPath(), v.getMessage());
        }
    }

    /**
     * Computes a set of formatted messages from the given typed set of {@link ConstraintViolation ConstraintViolations}.
     *
     * @param violations the constraint violations to format
     * @return a new set containing the formatted messages
     * @param <T> the generic type of the constraint violations
     */
    public static <T> Collection<String> format(Set<ConstraintViolation<T>> violations) {
        final SortedSet<String> errors = new TreeSet<>();
        for (ConstraintViolation<?> v : violations) {
            errors.add(format(v));
        }
        return errors;
    }

    /**
     * Computes a set of formatted messages from the given untyped set of {@link ConstraintViolation ConstraintViolations}.
     *
     * @param violations the constraint violations to format
     * @return a new set containing the formatted messages
     */
    public static Collection<String> formatUntyped(Set<ConstraintViolation<?>> violations) {
        final SortedSet<String> errors = new TreeSet<>();
        for (ConstraintViolation<?> v : violations) {
            errors.add(format(v));
        }
        return errors;
    }

    /**
     * Copies a set of {@link ConstraintViolation ConstraintViolations}.
     *
     * @param violations the source set
     * @return a new {@link HashSet} containing the violations from the source set
     * @param <T> the generic type of the constraint violations
     */
    public static <T> Set<ConstraintViolation<?>> copyOf(Set<ConstraintViolation<T>> violations) {
        return new HashSet<>(violations);
    }
}
