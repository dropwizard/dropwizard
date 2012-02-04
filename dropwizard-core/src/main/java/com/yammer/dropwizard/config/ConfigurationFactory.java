package com.yammer.dropwizard.config;

import com.google.common.collect.ImmutableList;
import com.yammer.dropwizard.json.Json;
import com.yammer.dropwizard.validation.Validator;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.Module;

import java.io.File;
import java.io.IOException;

public class ConfigurationFactory<T> {
    public static <T> ConfigurationFactory<T> forClass(Class<T> klass, Validator validator, Iterable<Module> modules) {
        return new ConfigurationFactory<T>(klass, validator, modules);
    }

    public static <T> ConfigurationFactory<T> forClass(Class<T> klass, Validator validator) {
        return new ConfigurationFactory<T>(klass, validator, ImmutableList.<Module>of());
    }

    private final Class<T> klass;
    private final Json json;
    private final Validator validator;

    private ConfigurationFactory(Class<T> klass, Validator validator, Iterable<Module> modules) {
        this.klass = klass;
        this.json = new Json();
        json.enable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);
        for (Module module : modules) {
            json.registerModule(module);
        }
        this.validator = validator;
    }
    
    public T build(File file) throws IOException, ConfigurationException {
        final T config = parse(file);
        validate(file, config);
        return config;
    }

    private T parse(File file) throws IOException {
        if (file.getName().endsWith(".yaml") || file.getName().endsWith(".yml")) {
            return json.readYamlValue(file, klass);
        }
        return json.readValue(file, klass);
    }

    private void validate(File file, T config) throws ConfigurationException {
        final ImmutableList<String> errors = validator.validate(config);
        if (!errors.isEmpty()) {
            throw new ConfigurationException(file, errors);
        }
    }
}
