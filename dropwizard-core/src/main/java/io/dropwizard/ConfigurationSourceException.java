
package io.dropwizard;

import io.dropwizard.configuration.ConfigurationException;
import java.util.Collection;

/**
 * An exception to be thrown when the wrong type of configuration source is 
 * passed to MergedConfigurationFactory's build method.
 *
 * @author JAshe
 */
public class ConfigurationSourceException extends ConfigurationException {

    /**
     * Create a config source exception
     * 
     * @param path
     * @param errors 
     */
    public ConfigurationSourceException(String path, Collection<String> errors) {
        super(path, errors);
    }

    /**
     * Create a config source exception
     */
    ConfigurationSourceException() {
        super("", null);
    }
    
}
