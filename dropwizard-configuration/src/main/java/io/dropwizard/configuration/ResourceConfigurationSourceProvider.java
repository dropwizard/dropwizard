package io.dropwizard.configuration;

import java.io.IOException;
import java.io.InputStream;

public class ResourceConfigurationSourceProvider implements ConfigurationSourceProvider {
    @Override
    public InputStream open(String path) throws IOException {
        return getClass().getClassLoader().getResourceAsStream(path);
    }
}
