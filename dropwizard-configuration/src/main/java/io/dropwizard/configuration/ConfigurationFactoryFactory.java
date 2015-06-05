package io.dropwizard.configuration;

import javax.validation.Validator;

import com.fasterxml.jackson.databind.ObjectMapper;

public interface ConfigurationFactoryFactory<T> {
    ConfigurationFactory<T> create(Class<T> klass,
            Validator validator,
            ObjectMapper objectMapper,
            String propertyPrefix);
}
