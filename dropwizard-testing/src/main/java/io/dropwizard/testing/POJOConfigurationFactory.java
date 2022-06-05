package io.dropwizard.testing;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.Configuration;
import io.dropwizard.configuration.ConfigurationSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.DefaultObjectMapperFactory;

import java.io.File;

public class POJOConfigurationFactory<C extends Configuration>
    extends YamlConfigurationFactory<C> {
    protected final C configuration;
    
    public POJOConfigurationFactory(C cfg) {
        this(cfg, new DefaultObjectMapperFactory().newObjectMapper());
    }

    @SuppressWarnings("unchecked")
    public POJOConfigurationFactory(C cfg, ObjectMapper objectMapper) {
        super((Class<C>) cfg.getClass(), null, objectMapper, "dw");
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
