package io.dropwizard.configuration;

import java.io.File;
import java.io.IOException;

/**
 * A generic interface for constructing a configuration object.
 *
 * @param <T> the type of the configuration objects to produce
 */
public interface ConfigurationFactory<T> {

    /**
     * Loads, parses, binds, and validates a configuration object.
     *
     * @param provider the provider to use for reading configuration files
     * @param path     the path of the configuration file
     * @return a validated configuration object
     * @throws IOException            if there is an error reading the file
     * @throws ConfigurationException if there is an error parsing or validating the file
     */
    T build(ConfigurationSourceProvider provider, String path) throws IOException, ConfigurationException;

    /**
     * Loads, parses, binds, and validates a configuration object from a file.
     *
     * @param file the path of the configuration file
     * @return a validated configuration object
     * @throws IOException            if there is an error reading the file
     * @throws ConfigurationException if there is an error parsing or validating the file
     */
    default T build(File file) throws IOException, ConfigurationException {
        return build(new FileConfigurationSourceProvider(), file.toString());
    }

    /**
     * Loads, parses, binds, and validates a configuration object from an empty document.
     *
     * @return a validated configuration object
     * @throws IOException            if there is an error reading the file
     * @throws ConfigurationException if there is an error parsing or validating the file
     */
    T build() throws IOException, ConfigurationException;
}
