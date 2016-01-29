package io.dropwizard.jersey.validation;

import com.google.common.collect.ImmutableList;
import io.dropwizard.validation.ConstraintViolations;
import io.dropwizard.validation.Validated;
import org.glassfish.jersey.server.internal.inject.ConfiguredValidator;
import org.glassfish.jersey.server.model.Invocable;
import org.glassfish.jersey.server.model.Parameter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.Validator;
import javax.validation.executable.ExecutableValidator;
import javax.validation.groups.Default;
import javax.validation.metadata.BeanDescriptor;
import javax.ws.rs.WebApplicationException;
import java.util.Arrays;
import java.util.Set;

import static java.util.Objects.requireNonNull;

public class DropwizardConfiguredValidator implements ConfiguredValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(DropwizardConfiguredValidator.class);

    private final Validator validator;

    public DropwizardConfiguredValidator(Validator validator) {
        this.validator = requireNonNull(validator);
    }

    @Override
    public void validateResourceAndInputParams(Object resource, final Invocable invocable, Object[] objects)
            throws ConstraintViolationException {
        final Class<?>[] groups = getGroup(invocable);
        final Set<ConstraintViolation<Object>> violations =
            forExecutables().validateParameters(resource, invocable.getHandlingMethod(), objects, groups);
        if (!violations.isEmpty()) {
            throw new JerseyViolationException(violations, invocable);
        }
    }

    /**
     * If the request entity is annotated with {@link Validated} then run
     * validations in the specified constraint group else validate with the
     * {@link Default} group
     */
    private Class<?>[] getGroup(Invocable invocable) {
        final ImmutableList.Builder<Class<?>[]> builder = ImmutableList.builder();
        for (Parameter parameter : invocable.getParameters()) {
            if (parameter.isAnnotationPresent(Validated.class)) {
                builder.add(parameter.getAnnotation(Validated.class).value());
            }
        }

        final ImmutableList<Class<?>[]> groups = builder.build();
        switch (groups.size()) {
            // No parameters were annotated with Validated, so validate under the default group
            case 0: return new Class<?>[] {Default.class};

            // A single parameter was annotated with Validated, so use their group
            case 1: return groups.get(0);

            // Multiple parameters were annotated with Validated, so we must check if
            // all groups are equal to each other, if not, throw an exception because
            // the validator is unable to handle parameters validated under different
            // groups. If the parameters have the same group, we can grab the first
            // group.
            default:
                for (int i = 0; i < groups.size(); i++) {
                    for (int j = i; j < groups.size(); j++) {
                        if (!Arrays.deepEquals(groups.get(i), groups.get(j))) {
                            throw new WebApplicationException("Parameters must have the same validation groups in " +
                                invocable.getHandlingMethod().getName(), 500);
                        }
                    }
                }
                return groups.get(0);
        }
    }

    @Override
    public void validateResult(Object resource, Invocable invocable, Object returnValue)
            throws ConstraintViolationException {
        // If the Validated annotation is on a method, then validate the response with
        // the specified constraint group.
        final Class<?>[] groups;
        if (invocable.getHandlingMethod().isAnnotationPresent(Validated.class)) {
            groups = invocable.getHandlingMethod().getAnnotation(Validated.class).value();
        } else {
            groups = new Class<?>[]{Default.class};
        }

        final Set<ConstraintViolation<Object>> violations =
            forExecutables().validateReturnValue(resource, invocable.getHandlingMethod(), returnValue, groups);
        if (!violations.isEmpty()) {
            LOGGER.trace("Response validation failed: {}", ConstraintViolations.copyOf(violations));
            throw new JerseyViolationException(violations, invocable);
        }
    }

    @Override
    public <T> Set<ConstraintViolation<T>> validate(T t, Class<?>... classes) {
        return validator.validate(t, classes);
    }

    @Override
    public <T> Set<ConstraintViolation<T>> validateProperty(T t, String s, Class<?>... classes) {
        return validator.validateProperty(t, s, classes);
    }

    @Override
    public <T> Set<ConstraintViolation<T>> validateValue(Class<T> aClass, String s, Object o, Class<?>... classes) {
        return validator.validateValue(aClass, s, o, classes);
    }

    @Override
    public BeanDescriptor getConstraintsForClass(Class<?> aClass) {
        return validator.getConstraintsForClass(aClass);
    }

    @Override
    public <T> T unwrap(Class<T> aClass) {
        return validator.unwrap(aClass);
    }

    @Override
    public ExecutableValidator forExecutables() {
        return validator.forExecutables();
    }
}
