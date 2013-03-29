package com.yammer.dropwizard.config.provider;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Implementation of the {@link ConfigurationSourceProvider} that reads the configuration
 * from a {@link URL}.
 */
public class UrlConfigurationSourceProvider implements ConfigurationSourceProvider {

    @Override
    public InputStream create(String configurationPath) throws IOException {
        if(configurationPath == null) {
            throw new IllegalArgumentException("URL cannot be null.");
        }

        return new URL(configurationPath).openStream();
    }
}
