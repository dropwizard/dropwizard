
package io.dropwizard;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.ConfigurationFactoryFactory;
import javax.validation.Validator;

/**
 * A ConfigurationFactoryFactory that will create and return a 
 * MergedConfigurationFactory class.
 * 
 * @author JAshe
 * @param <T>
 */
public class MergedConfigurationFactoryFactory<T> implements ConfigurationFactoryFactory<T> {

    @Override
    public MergedConfigurationFactory<T> create(
            Class<T>     klass,
            Validator    validator, 
            ObjectMapper objectMapper,
            String       propertyPrefix) {
        return new MergedConfigurationFactory<>(klass, validator, objectMapper, propertyPrefix);
    }

    
}
