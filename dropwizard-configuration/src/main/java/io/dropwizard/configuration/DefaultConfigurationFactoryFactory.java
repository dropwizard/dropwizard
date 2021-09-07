package io.dropwizard.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.validation.Validator;

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
     * the configuration.
     *
     * @param objectMapper template to be configured
     * @return configured object mapper
     */
    protected ObjectMapper configureObjectMapper(ObjectMapper objectMapper) {
        return objectMapper;
    }

}
