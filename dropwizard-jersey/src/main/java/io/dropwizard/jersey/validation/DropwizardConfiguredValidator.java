package io.dropwizard.jersey.validation;

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
import java.util.Set;

import static com.google.common.base.Preconditions.checkNotNull;

public class DropwizardConfiguredValidator implements ConfiguredValidator {
    private static final Logger LOGGER = LoggerFactory.getLogger(DropwizardConfiguredValidator.class);

    private final Validator validator;

    public DropwizardConfiguredValidator(Validator validator) {
        this.validator = checkNotNull(validator);
    }

    @Override
    public void validateResourceAndInputParams(Object resource, final Invocable invocable, Object[] objects) throws ConstraintViolationException {
        Class<?>[] groups = getGroup(invocable);
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
        for (Parameter parameter : invocable.getParameters()) {
            if (parameter.getSource().equals(Parameter.Source.UNKNOWN)) {
                if (parameter.isAnnotationPresent(Validated.class)) {
                    return parameter.getAnnotation(Validated.class).value();
                }
            }
        }
        return new Class<?>[] {Default.class};
    }

    @Override
    public void validateResult(Object resource, Invocable invocable, Object returnValue) throws ConstraintViolationException {
        // If the Validated annotation is on a method, then validate the response with
        // the specified constraint group.
        Class<?>[] groups = {Default.class};
        if (invocable.getHandlingMethod().isAnnotationPresent(Validated.class)) {
            groups = invocable.getHandlingMethod().getAnnotation(Validated.class).value();
        }

        final Set<ConstraintViolation<Object>> violations =
            forExecutables().validateReturnValue(resource, invocable.getHandlingMethod(), returnValue, groups);
        if (!violations.isEmpty()) {
            Set<ConstraintViolation<?>> constraintViolations = ConstraintViolations.copyOf(violations);
            LOGGER.trace("Response validation failed: {}", constraintViolations);
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
