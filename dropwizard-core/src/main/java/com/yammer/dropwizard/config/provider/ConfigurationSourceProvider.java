package com.yammer.dropwizard.config.provider;

import java.io.IOException;
import java.io.InputStream;

/**
 * An interface for objects that can create an {@link InputStream} to represent the service
 * configuration.
 */
public interface ConfigurationSourceProvider {
    /**
     * Returns an {@link InputStream} that contains the source of the configuration for the
     * service.
     *
     * @param path the path to the configuration
     * @return a {@link InputStream}
     * @throws IOException if there is an error reading the data at {@code path}
     */
    public InputStream create(String path) throws IOException;

}
