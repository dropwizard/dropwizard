package io.dropwizard.configuration;

import javax.annotation.Nullable;
import javax.validation.Validator;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;

/**
 * A factory class for loading YAML configuration files, binding them to configuration objects, and
 * validating their constraints. Allows for overriding configuration parameters from system properties.
 *
 * @param <T> the type of the configuration objects to produce
 */
public class YamlConfigurationFactory<T> extends BaseConfigurationFactory<T> {

    /**
     * Creates a new configuration factory for the given class.
     *
     * @param klass          the configuration class
     * @param validator      the validator to use
     * @param objectMapper   the Jackson {@link ObjectMapper} to use
     * @param propertyPrefix the system property name prefix used by overrides
     */
    public YamlConfigurationFactory(Class<T> klass,
                                    @Nullable Validator validator,
                                    ObjectMapper objectMapper,
                                    String propertyPrefix) {
        super(new YAMLFactory(), YAMLFactory.FORMAT_NAME_YAML, klass, validator, objectMapper, propertyPrefix);
    }
}
