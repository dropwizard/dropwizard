package io.dropwizard.jersey.validation;

import com.google.common.base.Optional;
import com.google.common.collect.Iterables;
import io.dropwizard.validation.ConstraintViolations;
import io.dropwizard.validation.ValidationMethod;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.commons.lang3.reflect.MethodUtils;

import javax.validation.ConstraintViolation;
import javax.validation.ElementKind;
import javax.validation.Path;
import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;

public class ConstraintMessage {
    /**
     * Gets the human friendly location of where the violation was raised.
     */
    public static String getMessage(ConstraintViolation<?> v) {
        final Optional<String> returnValueName = getMethodReturnValueName(v);
        if (returnValueName.isPresent()) {
            final String name = isValidationMethod(v) ?
                    StringUtils.substringBeforeLast(returnValueName.get(), ".") : returnValueName.get();
            return name + " " + v.getMessage();
        } else if (isValidationMethod(v)) {
            return ConstraintViolations.validationMethodFormatted(v);
        } else {
            final String name = getMemberName(v).or(v.getPropertyPath().toString());
            return name + " " + v.getMessage();
        }
    }

    /**
     * Gets a method parameter (or a parameter field) name, if the violation raised in it.
     */
    private static Optional<String> getMemberName(ConstraintViolation<?> violation) {
        final int size = Iterables.size(violation.getPropertyPath());
        if (size < 2) {
            return Optional.absent();
        }

        final Path.Node parent = Iterables.get(violation.getPropertyPath(), size - 2);
        final Path.Node member = Iterables.getLast(violation.getPropertyPath());
        final Class<?> resourceClass = violation.getLeafBean().getClass();
        switch (parent.getKind()) {
            case PARAMETER:
                Field field = FieldUtils.getDeclaredField(resourceClass, member.getName(), true);
                return getMemberName(field.getDeclaredAnnotations());
            case METHOD:
                List<Class<?>> params = parent.as(Path.MethodNode.class).getParameterTypes();
                Class<?>[] parcs = params.toArray(new Class<?>[params.size()]);
                Method method = MethodUtils.getAccessibleMethod(resourceClass, parent.getName(), parcs);

                int paramIndex = member.as(Path.ParameterNode.class).getParameterIndex();
                return getMemberName(method.getParameterAnnotations()[paramIndex]);
            default:
                return Optional.absent();
        }
    }

    /**
     * Gets the method return value name, if the violation is raised in it
     */
    private static Optional<String> getMethodReturnValueName(ConstraintViolation<?> violation) {
        int returnValueNames = -1;

        final StringBuilder result = new StringBuilder("server response");
        for (Path.Node node : violation.getPropertyPath()) {
            if (node.getKind().equals(ElementKind.RETURN_VALUE)) {
                returnValueNames = 0;
            } else if (returnValueNames >= 0) {
                result.append(returnValueNames++ == 0 ? " " : ".").append(node);
            }
        }

        return returnValueNames >= 0 ? Optional.of(result.toString()) : Optional.<String>absent();
    }

    /**
     * Derives member's name and type from it's annotations
     */
    private static Optional<String> getMemberName(Annotation[] memberAnnotations) {
        for (Annotation a : memberAnnotations) {
            if (a instanceof QueryParam) {
                return Optional.of("query param " + ((QueryParam) a).value());
            } else if (a instanceof PathParam) {
                return Optional.of("path param " + ((PathParam) a).value());
            } else if (a instanceof HeaderParam) {
                return Optional.of("header " + ((HeaderParam) a).value());
            } else if (a instanceof CookieParam) {
                return Optional.of("cookie " + ((CookieParam) a).value());
            } else if (a instanceof FormParam) {
                return Optional.of("form field " + ((FormParam) a).value());
            } else if (a instanceof Context) {
                return Optional.of("context");
            } else if (a instanceof MatrixParam) {
                return Optional.of("matrix param " + ((MatrixParam) a).value());
            }
        }

        return Optional.absent();
    }

    private static boolean isValidationMethod(ConstraintViolation<?> v) {
        return v.getConstraintDescriptor().getAnnotation() instanceof ValidationMethod;
    }
}
