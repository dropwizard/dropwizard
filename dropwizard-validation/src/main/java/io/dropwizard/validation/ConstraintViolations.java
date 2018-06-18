package io.dropwizard.validation;

import javax.validation.ConstraintViolation;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

public class ConstraintViolations {
    private ConstraintViolations() { /* singleton */ }

    public static <T> String format(ConstraintViolation<T> v) {
        if (v.getConstraintDescriptor().getAnnotation() instanceof ValidationMethod) {
            return v.getMessage();
        } else {
            return String.format("%s %s", v.getPropertyPath(), v.getMessage());
        }
    }

    public static <T> Collection<String> format(Set<ConstraintViolation<T>> violations) {
        final SortedSet<String> errors = new TreeSet<>();
        for (ConstraintViolation<?> v : violations) {
            errors.add(format(v));
        }
        return errors;
    }

    public static Collection<String> formatUntyped(Set<ConstraintViolation<?>> violations) {
        final SortedSet<String> errors = new TreeSet<>();
        for (ConstraintViolation<?> v : violations) {
            errors.add(format(v));
        }
        return errors;
    }

    public static <T> Set<ConstraintViolation<?>> copyOf(Set<ConstraintViolation<T>> violations) {
        return new HashSet<>(violations);
    }
}
