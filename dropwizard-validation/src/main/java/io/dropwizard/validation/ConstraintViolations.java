package io.dropwizard.validation;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Ordering;
import com.google.common.collect.Sets;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ElementKind;
import javax.validation.Path;
import java.util.Set;

public class ConstraintViolations {
    private ConstraintViolations() { /* singleton */ }

    public static <T> String format(ConstraintViolation<T> v) {
        if (v.getConstraintDescriptor().getAnnotation() instanceof ValidationMethod) {
            final ImmutableList<Path.Node> nodes = ImmutableList.copyOf(v.getPropertyPath());
            final ImmutableList<Path.Node> usefulNodes = nodes.subList(0, nodes.size() - 1);
            final String msg = v.getMessage().startsWith(".") ? "%s%s" : "%s %s";
            return String.format(msg,
                                 Joiner.on('.').join(usefulNodes),
                                 v.getMessage()).trim();
        } else {
            return String.format("%s %s", v.getPropertyPath(), v.getMessage());
        }
    }

    public static <T> ImmutableList<String> format(Set<ConstraintViolation<T>> violations) {
        final Set<String> errors = Sets.newHashSet();
        for (ConstraintViolation<?> v : violations) {
            errors.add(format(v));
        }
        return ImmutableList.copyOf(Ordering.natural().sortedCopy(errors));
    }

    public static ImmutableList<String> formatUntyped(Set<ConstraintViolation<?>> violations) {
        final Set<String> errors = Sets.newHashSet();
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

    public static int determineStatus(ConstraintViolationException exception) {
        // Detect where the constraint validation occurred so we can return an appropriate status
        // code. If the constraint failed with a *Param annotation, return a bad request. If it
        // failed validating the return value, return internal error. Else return unprocessable
        // entity.
        Set<ConstraintViolation<?>> violations = exception.getConstraintViolations();
        if (violations.size() > 0) {
            ConstraintViolation<?> violation = violations.iterator().next();
            boolean isReturnValue = false;
            boolean isArgument = false;

            // A return value can only occur at the last path, but a parameter
            // can occur anywhere, such as a @BeanParam that has validations.
            for (Path.Node node : violation.getPropertyPath()) {
                isArgument |= node.getKind().equals(ElementKind.PARAMETER);
                isReturnValue = node.getKind().equals(ElementKind.RETURN_VALUE);
            }

            if (isReturnValue) {
                return 500;
            } else if (isArgument) {
                return 400;
            }
        }

        return 422;
    }
}
