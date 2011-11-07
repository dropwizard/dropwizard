package com.yammer.dropwizard.config;

import com.google.common.base.Charsets;
import com.google.common.io.Files;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.Constructor;
import org.yaml.snakeyaml.error.YAMLException;
import org.yaml.snakeyaml.introspector.BeanAccess;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.Set;

public class ConfigurationFactory<T> {
    private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();
    
    private final Class<T> klass;

    public ConfigurationFactory(Class<T> klass) {
        this.klass = klass;
    }
    
    public T build(File file) throws IOException, ConfigurationException, YAMLException {
        final BufferedReader reader = Files.newReader(file, Charsets.UTF_8);
        try {
            final Yaml yaml = buildYamlParser();
            final T config = loadYaml(reader, yaml);
            if (config == null) {
                throw new YAMLException("Can't load " + file + "; the file is empty.");
            }
            validate(file, config);
            return config;
        } finally {
            reader.close();
        }
    }

    private void validate(File file, T config) throws ConfigurationException {
        final Validator validator = VALIDATOR_FACTORY.getValidator();
        final Set<ConstraintViolation<T>> violations = validator.validate(config);
        if (!violations.isEmpty()) {
            throw new ConfigurationException(file, violations);

        }
    }

    @SuppressWarnings("unchecked")
    private T loadYaml(BufferedReader reader, Yaml yaml) {
        return (T) yaml.load(reader);
    }

    private Yaml buildYamlParser() {
        final Yaml yaml = new Yaml(new Constructor(klass));
        yaml.setBeanAccess(BeanAccess.FIELD);
        return yaml;
    }
}
