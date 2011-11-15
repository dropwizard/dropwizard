package com.yammer.dropwizard.config;

import com.google.common.base.Charsets;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Files;
import com.yammer.dropwizard.util.Validator;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.introspector.BeanAccess;

import java.io.BufferedReader;
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
    
    public T build(File file) throws IOException, ConfigurationException, YAMLException {
        final BufferedReader reader = Files.newReader(file, Charsets.UTF_8);
        try {
            final Yaml yaml = buildYamlParser();
            final T config = loadYaml(reader, yaml);
            validate(file, config);
            return config;
        } finally {
            reader.close();
        }
    }

    private T defaultInstance() {
        try {
            return klass.newInstance();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void validate(File file, T config) throws ConfigurationException {
        final ImmutableList<String> errors = validator.validate(config);
        if (!errors.isEmpty()) {
            throw new ConfigurationException(file, errors);
        }
    }

    @SuppressWarnings("unchecked")
    private T loadYaml(BufferedReader reader, Yaml yaml) {
        final T config = (T) yaml.load(reader);
        if (config == null) {
            return defaultInstance();
        }
        return config;
    }

    private Yaml buildYamlParser() {
        final Yaml yaml = new Yaml(new Constructor(klass));
        yaml.setBeanAccess(BeanAccess.FIELD);
        return yaml;
    }
}
