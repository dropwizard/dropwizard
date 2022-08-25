package io.dropwizard.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.validation.Validator;

/**
 * The default implementation for the {@link ConfigurationFactoryFactory} interface. An instance of this class constructs a new {@link YamlConfigurationFactory}.
 *
 * @param <T> the type of the configuration objects to produce
 */
public class DefaultConfigurationFactoryFactory<T> implements ConfigurationFactoryFactory<T> {
    @Override
    public ConfigurationFactory<T> create(
            Class<T>     klass,
            Validator    validator,
            ObjectMapper objectMapper,
            String       propertyPrefix) {
        return new YamlConfigurationFactory<>(
            klass,
            validator,
            configureObjectMapper(objectMapper.copy()),
            propertyPrefix);
    }

    /**
     * Provides additional configuration for the {@link ObjectMapper} used to read
     * the configuration. By default {@link DeserializationFeature#FAIL_ON_UNKNOWN_PROPERTIES}
     * is enabled to protect against misconfiguration.
     *
     * @param objectMapper template to be configured
     * @return configured object mapper
     */
    protected ObjectMapper configureObjectMapper(ObjectMapper objectMapper) {
        return objectMapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
    }

}
