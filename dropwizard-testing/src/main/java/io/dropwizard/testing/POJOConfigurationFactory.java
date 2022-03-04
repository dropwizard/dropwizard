package io.dropwizard.testing;

import com.fasterxml.jackson.databind.JsonNode;
import io.dropwizard.core.Configuration;
import io.dropwizard.configuration.ConfigurationSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;

import java.io.File;

public class POJOConfigurationFactory<C extends Configuration>
    extends YamlConfigurationFactory<C> {
    protected final C configuration;

    @SuppressWarnings("unchecked")
    public POJOConfigurationFactory(C cfg) {
        super((Class<C>) cfg.getClass(), null, Jackson.newObjectMapper(), "dw");
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
