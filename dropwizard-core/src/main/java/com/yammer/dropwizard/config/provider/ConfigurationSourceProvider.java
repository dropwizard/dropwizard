package com.yammer.dropwizard.config.provider;

import java.io.IOException;
import java.io.InputStream;

/**
 * An interface for objects that can create an {@link InputStream} to represent
 * the service configuration.
 */
public interface ConfigurationSourceProvider {

    /**
     * Returns an {@link InputStream} that contains the source of the configuration for the service
     *
     * @param configurationPath the
     * @return a {@link InputStream}
     * @throws IOException If something goes wrong during creation of the InputStream
     */
    public InputStream create(String configurationPath) throws IOException;

}
