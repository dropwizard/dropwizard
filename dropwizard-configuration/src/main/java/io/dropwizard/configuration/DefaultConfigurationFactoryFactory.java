package io.dropwizard.configuration;

import javax.validation.Validator;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DefaultConfigurationFactoryFactory<T> implements ConfigurationFactoryFactory<T> {
    @Override
    public ConfigurationFactory<T> create(
            Class<T>     klass,
            Validator    validator, 
            ObjectMapper objectMapper,
            String       propertyPrefix) {
        return new ConfigurationFactory<>(klass, validator, objectMapper, propertyPrefix);
    }
}
