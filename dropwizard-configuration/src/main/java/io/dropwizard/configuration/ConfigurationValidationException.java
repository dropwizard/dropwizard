package io.dropwizard.configuration;

import io.dropwizard.validation.ConstraintViolations;
import jakarta.validation.ConstraintViolation;

import java.util.Set;

/**
 * An exception thrown where there is an error validating a configuration object.
 */
public class ConfigurationValidationException extends ConfigurationException {
    private static final long serialVersionUID = 5325162099634227047L;

    /**
     * The constraint violation occurred during configuration validation.
     */
    private final Set<ConstraintViolation<?>> constraintViolations;

    /**
     * Creates a new ConfigurationException for the given path with the given errors.
     *
     * @param path      the bad configuration path
     * @param errors    the errors in the path
     * @param <T> the type of the root bean of a constraint violation
     */
    public <T> ConfigurationValidationException(String path, Set<ConstraintViolation<T>> errors) {
        super(path, ConstraintViolations.format(errors));
        this.constraintViolations = ConstraintViolations.copyOf(errors);
    }

    /**
     * Returns the set of constraint violations in the configuration.
     *
     * @return the set of constraint violations
     */
    public Set<ConstraintViolation<?>> getConstraintViolations() {
        return constraintViolations;
    }
}
