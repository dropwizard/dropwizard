package io.dropwizard.testing;

import java.io.File;

import com.fasterxml.jackson.databind.JsonNode;

import io.dropwizard.Configuration;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.configuration.ConfigurationSourceProvider;

public class POJOConfigurationFactory<C extends Configuration>
    extends ConfigurationFactory<C> {
    protected final C configuration;

    @SuppressWarnings("unchecked")
    public POJOConfigurationFactory(C cfg) {
        super((Class<C>) cfg.getClass(), null, null, null);
        configuration = cfg;
    }

    @Override
    public C build(ConfigurationSourceProvider provider, String path) {
        return configuration;
    }

    @Override
    public C build(File file) {
        return configuration;
    }

    @Override
    public C build() {
        return configuration;
    }

    @Override
    protected C build(JsonNode node, String path) {
        return configuration;
    }
}
