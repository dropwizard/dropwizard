package io.dropwizard.configuration;

import javax.validation.Validator;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A factory class for loading JSON configuration files, binding them to configuration objects, and
 * validating their constraints. Allows for overriding configuration parameters from system properties.
 *
 * @param <T> the type of the configuration objects to produce
 */
public class JsonConfigurationFactory<T> extends BaseConfigurationFactory<T> {

    /**
     * Creates a new configuration factory for the given class.
     *
     * @param klass          the configuration class
     * @param validator      the validator to use
     * @param objectMapper   the Jackson {@link ObjectMapper} to use
     * @param propertyPrefix the system property name prefix used by overrides
     */
    public JsonConfigurationFactory(Class<T> klass,
                                    Validator validator,
                                    ObjectMapper objectMapper,
                                    String propertyPrefix) {
        super(objectMapper.getFactory(), JsonFactory.FORMAT_NAME_JSON, klass, validator, objectMapper, propertyPrefix);
    }
}
