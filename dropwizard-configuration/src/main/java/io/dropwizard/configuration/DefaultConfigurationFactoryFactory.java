package io.dropwizard.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.validation.Validator;

public class DefaultConfigurationFactoryFactory<T> implements ConfigurationFactoryFactory<T> {
    @Override
    public ConfigurationFactory<T> create(
            Class<T>     klass,
            Validator    validator,
            ObjectMapper objectMapper,
            String       propertyPrefix) {
        return new YamlConfigurationFactory<>(klass, validator, objectMapper, propertyPrefix);
    }
}
