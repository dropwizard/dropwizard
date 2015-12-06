package io.dropwizard.validation;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;

import javax.validation.ConstraintViolation;
import javax.validation.ElementKind;
import javax.validation.Path;
import java.util.HashSet;
import java.util.Set;

public class ConstraintViolations {
    private static final Joiner DOT_JOINER = Joiner.on('.');

    private ConstraintViolations() { /* singleton */ }

    public static <T> String format(ConstraintViolation<T> v) {
        if (v.getConstraintDescriptor().getAnnotation() instanceof ValidationMethod) {
            return validationMethodFormatted(v);
        } else {
            return String.format("%s %s", v.getPropertyPath(), v.getMessage());
        }
    }

    public static <T> String validationMethodFormatted(ConstraintViolation<T> v) {
        final ImmutableList<Path.Node> nodes = ImmutableList.copyOf(v.getPropertyPath());

        // It is possible that a BeanParam may contain a ValidationMethod, and in which case, it has
        // name that is not client friendly (ie. <function>.<arg index>.<validation method
        // message>.) This will trim it off.
        int paramIndex = -1;
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i).getKind() == ElementKind.PARAMETER) {
                paramIndex = i;
            }
        }

        final String usefulNodes = DOT_JOINER.join(nodes.subList(paramIndex + 1, nodes.size() - 1));
        final String msg = usefulNodes + (v.getMessage().startsWith(".") ? "" : " ") + v.getMessage();
        return msg.trim();
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
