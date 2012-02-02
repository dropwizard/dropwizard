package com.yammer.dropwizard.config;

import com.google.common.collect.ImmutableList;
import com.yammer.dropwizard.json.Yaml;
import com.yammer.dropwizard.validation.Validator;

import java.io.File;
import java.io.IOException;

public class ConfigurationFactory<T> {
    public static <T> ConfigurationFactory<T> forClass(Class<T> klass, Validator validator) {
        return new ConfigurationFactory<T>(klass, validator);
    }

    private final Class<T> klass;
    private final Validator validator;

    public ConfigurationFactory(Class<T> klass, Validator validator) {
        this.klass = klass;
        this.validator = validator;
    }
    
    public T build(File file) throws IOException, ConfigurationException {
        final T config = new Yaml(file).read(klass);
        validate(file, config);
        return config;
    }

    private void validate(File file, T config) throws ConfigurationException {
        final ImmutableList<String> errors = validator.validate(config);
        if (!errors.isEmpty()) {
            throw new ConfigurationException(file, errors);
        }
    }
}
