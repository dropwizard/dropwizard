package io.dropwizard.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.validation.Validator;

/**
 * A generic interface for constructing a configuration factory that can create configuration objects.
 *
 * @param <T> the type of the configuration objects to produce
 */
public interface ConfigurationFactoryFactory<T> {
    /**
     * Creates a new configuration factory for the given class.
     *
     * @param klass          the configuration class
     * @param validator      the validator to use
     * @param objectMapper   the Jackson {@link ObjectMapper} to use
     * @param propertyPrefix the system property name prefix used by overrides
     * @return the new configuration factory
     */
    ConfigurationFactory<T> create(Class<T> klass,
            Validator validator,
            ObjectMapper objectMapper,
            String propertyPrefix);
}
