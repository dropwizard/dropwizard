package io.dropwizard.configuration;

import java.io.IOException;
import java.io.InputStream;

/**
 * An interface for objects that can create an {@link InputStream} to represent the application
 * configuration.
 */
public interface ConfigurationSourceProvider {
    /**
     * Returns an {@link InputStream} that contains the source of the configuration for the
     * application. The caller is responsible for closing the result.
     *
     * @param path the path to the configuration
     * @return an {@link InputStream}
     * @throws IOException if there is an error reading the data at {@code path}
     */
    InputStream open(String path) throws IOException;

}
