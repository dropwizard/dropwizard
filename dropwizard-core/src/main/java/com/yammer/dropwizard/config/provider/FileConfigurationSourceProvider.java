package com.yammer.dropwizard.config.provider;

import java.io.*;

/**
 * Implementation of the {@link ConfigurationSourceProvider} that reads the configuration
 * from the local file system.
 */
public class FileConfigurationSourceProvider implements ConfigurationSourceProvider {

    @Override
    public InputStream create(String configurationPath) throws IOException {
        if(configurationPath == null) {
            throw new IllegalArgumentException("Configuration file path cannot be null.");
        }

        final File file = new File(configurationPath);
        if (!file.exists()) {
            throw new FileNotFoundException("File " + file + " not found");
        }

        return new FileInputStream(file);
    }
}
