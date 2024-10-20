package io.dropwizard.jersey.validation;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.dropwizard.validation.ValidationMethod;
import io.dropwizard.validation.selfvalidating.SelfValidating;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ElementKind;
import jakarta.validation.Path;
import jakarta.validation.metadata.ConstraintDescriptor;
import org.glassfish.jersey.server.model.Invocable;
import org.glassfish.jersey.server.model.Parameter;

import java.lang.annotation.Annotation;
import java.time.Duration;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.glassfish.jersey.model.Parameter.Source.BEAN_PARAM;
import static org.glassfish.jersey.model.Parameter.Source.UNKNOWN;

public class ConstraintMessage {

    private static final Cache<AbstractMap.SimpleImmutableEntry<Path, ? extends ConstraintDescriptor<?>>, String> PREFIX_CACHE =
            Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofHours(1))
            .build();

    private ConstraintMessage() {
    }

    /**
     * Gets the human friendly location of where the violation was raised.
     */
    public static String getMessage(ConstraintViolation<?> v, Invocable invocable) {
        final AbstractMap.SimpleImmutableEntry<Path, ? extends ConstraintDescriptor<?>> of =
            new AbstractMap.SimpleImmutableEntry<>(v.getPropertyPath(), v.getConstraintDescriptor());
        final String cachePrefix = PREFIX_CACHE.get(of, k -> calculatePrefix(v, invocable));
        return cachePrefix + v.getMessage();
    }

    private static String stripLastComponent(String str) {
        int pos = str.lastIndexOf('.');
        return pos == -1 ? str : str.substring(0, pos);
    }

    private static String calculatePrefix(ConstraintViolation<?> v, Invocable invocable) {
        final Optional<String> returnValueName = getMethodReturnValueName(v);
        if (returnValueName.isPresent()) {
            final String name = isValidationMethod(v) ?
                    stripLastComponent(returnValueName.get()) : returnValueName.get();
            return name + " ";
        }

        // Take the message specified in a ValidationMethod or SelfValidation
        // annotation if it is what caused the violation.
        if (isValidationMethod(v) || isSelfValidating(v)) {
            return "";
        }

        final Optional<String> entity = isRequestEntity(v, invocable);
        if (entity.isPresent()) {
            // A present entity means that the request body failed validation but
            // if the request entity is simple (e.g. byte[], String, etc.), the entity
            // string will be empty, so prepend a message about the request body
            return entity.filter(e -> !e.isEmpty())
                .orElse("The request body")
                + " ";
        }

        // Check if the violation occurred on a *Param annotation and if so,
        // return a human friendly error (e.g. "Query param xxx may not be null")
        final Optional<String> memberName = getMemberName(v, invocable);
        return memberName.map(s -> s + " ").orElseGet(() -> v.getPropertyPath() + " ");

    }

    /**
     * Determines if constraint violation occurred in the request entity. If it did, return a client
     * friendly string representation of where the error occurred (e.g. "patient.name")
     */
    public static Optional<String> isRequestEntity(ConstraintViolation<?> violation, Invocable invocable) {
        final Path.Node parent = StreamSupport.stream(violation.getPropertyPath().spliterator(), false)
                .skip(1L)
                .findFirst()
                .orElse(null);
        if (parent == null) {
            return Optional.empty();
        }
        final List<Parameter> parameters = invocable.getParameters();

        if (parent.getKind() == ElementKind.PARAMETER) {
            final Parameter param = parameters.get(parent.as(Path.ParameterNode.class).getParameterIndex());
            if (param.getSource().equals(UNKNOWN)) {
                final String path = StreamSupport.stream(violation.getPropertyPath().spliterator(), false)
                        .skip(2L)
                        .map(Path.Node::toString)
                        .collect(Collectors.joining("."));

                return Optional.of(path);
            }
        }

        return Optional.empty();
    }

    /**
     * Gets a method parameter (or a parameter field) name, if the violation raised in it.
     */
    private static Optional<String> getMemberName(ConstraintViolation<?> violation, Invocable invocable) {
        final List<Path.Node> propertyPath = new ArrayList<>();
        violation.getPropertyPath().iterator().forEachRemaining(propertyPath::add);
        final int size = propertyPath.size();
        if (size < 2) {
            return Optional.empty();
        }

        final Path.Node parent = propertyPath.get(size - 2);
        final Path.Node member = propertyPath.get(size - 1);
        switch (parent.getKind()) {
            case PARAMETER:
                // Constraint violation most likely failed with a BeanParam
                final List<Parameter> parameters = invocable.getParameters();
                final Parameter param = parameters.get(parent.as(Path.ParameterNode.class).getParameterIndex());

                // Extract the failing *Param annotation inside the Bean Param
                if (param.getSource().equals(BEAN_PARAM)) {
                    return getFieldAnnotations(param.getRawType(), member.getName())
                        .flatMap(JerseyParameterNameProvider::getParameterNameFromAnnotations);
                }
                break;
            case METHOD:
                return Optional.of(member.getName());
            default:
                break;
        }
        return Optional.empty();
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

        return returnValueNames >= 0 ? Optional.of(result.toString()) : Optional.empty();
    }

    private static boolean isValidationMethod(ConstraintViolation<?> v) {
        return v.getConstraintDescriptor().getAnnotation() instanceof ValidationMethod;
    }

    private static boolean isSelfValidating(ConstraintViolation<?> v) {
        return v.getConstraintDescriptor().getAnnotation() instanceof SelfValidating;
    }

    /**
     * Given a set of constraint violations and a Jersey {@link Invocable} where the constraint
     * occurred, determine the  HTTP Status code for the response. A return value violation is an
     * internal server error, an invalid request body is unprocessable entity, and any params that
     * are invalid means a bad request
     */
    public static <T extends ConstraintViolation<?>> int determineStatus(Set<T> violations, Invocable invocable) {
        if (!violations.isEmpty()) {
            final ConstraintViolation<?> violation = violations.iterator().next();
            for (Path.Node node : violation.getPropertyPath()) {
                switch (node.getKind()) {
                    case RETURN_VALUE:
                        return 500;
                    case PARAMETER:
                        // Now determine if the parameter is the request entity
                        final int index = node.as(Path.ParameterNode.class).getParameterIndex();
                        final Parameter parameter = invocable.getParameters().get(index);
                        return parameter.getSource().equals(UNKNOWN) ? 422 : 400;
                    default:
                        continue;
                }
            }
        }

        // This shouldn't hit, but if it does, we'll return an unprocessable entity
        return 422;
    }

    private static Optional<Annotation[]> getFieldAnnotations(Class klass, String name) {
        try {
            return Optional.of(klass.getDeclaredField(name).getDeclaredAnnotations());
        } catch (NoSuchFieldException e) {
            return Optional.ofNullable(klass.getSuperclass())
                .flatMap(superClass -> getFieldAnnotations(superClass, name));
        }
    }
}
