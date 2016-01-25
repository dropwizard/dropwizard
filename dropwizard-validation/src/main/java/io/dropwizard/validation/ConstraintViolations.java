package io.dropwizard.validation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;

import javax.validation.ConstraintViolation;
import java.util.HashSet;
import java.util.Set;

public class ConstraintViolations {
    private ConstraintViolations() { /* singleton */ }

    public static <T> String format(ConstraintViolation<T> v) {
        if (v.getConstraintDescriptor().getAnnotation() instanceof ValidationMethod) {
            return v.getMessage();
        } else {
            return String.format("%s %s", v.getPropertyPath(), v.getMessage());
        }
    }

    public static <T> ImmutableList<String> format(Set<ConstraintViolation<T>> violations) {
        final Set<String> errors = new HashSet<>();
        for (ConstraintViolation<?> v : violations) {
            errors.add(format(v));
        }
        return ImmutableList.copyOf(Ordering.natural().sortedCopy(errors));
    }

    public static ImmutableList<String> formatUntyped(Set<ConstraintViolation<?>> violations) {
        final Set<String> errors = new HashSet<>();
        for (ConstraintViolation<?> v : violations) {
            errors.add(format(v));
        }
        return ImmutableList.copyOf(Ordering.natural().sortedCopy(errors));
    }

    public static <T> ImmutableSet<ConstraintViolation<?>> copyOf(Set<ConstraintViolation<T>> violations) {
        final ImmutableSet.Builder<ConstraintViolation<?>> builder = ImmutableSet.builder();
        for (ConstraintViolation<T> violation : violations) {
            builder.add(violation);
        }
        return builder.build();
    }
}
