package com.yammer.dropwizard.config;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.yammer.dropwizard.json.Json;
import com.yammer.dropwizard.validation.Validator;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class ConfigurationFactory<T> {

    private static final String PROPERTY_PREFIX = "dw.";

    public static <T> ConfigurationFactory<T> forClass(Class<T> klass, Validator validator, Iterable<Module> modules) {
        return new ConfigurationFactory<T>(klass, validator, modules);
    }

    public static <T> ConfigurationFactory<T> forClass(Class<T> klass, Validator validator) {
        return new ConfigurationFactory<T>(klass, validator, ImmutableList.<Module>of());
    }

    private final Class<T> klass;
    private final Json json;
    private final Validator validator;

    private ConfigurationFactory(Class<T> klass, Validator validator, Iterable<Module> modules) {
        this.klass = klass;
        this.json = new Json(new YAMLFactory());
        json.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        for (Module module : modules) {
            json.registerModule(module);
        }
        this.validator = validator;
    }
    
    public T build(File file) throws IOException, ConfigurationException {
        final JsonNode node = json.readValue(file, JsonNode.class);
        final String filename = file.toString();
        return build(node, filename);
    }

    public T build() throws IOException, ConfigurationException {
        return build(JsonNodeFactory.instance.objectNode(), "The default configuration");
    }

    private T build(JsonNode node, String filename) throws IOException, ConfigurationException {
        for (Map.Entry<Object, Object> pref : System.getProperties().entrySet()) {
            final String prefName = (String) pref.getKey();
            if (prefName.startsWith(PROPERTY_PREFIX)) {
                final String configName = prefName.substring(PROPERTY_PREFIX.length());
                addOverride(node, configName, System.getProperty(prefName));
            }
        }
        final T config = json.readValue(node, klass);
        validate(filename, config);
        return config;
    }

    private void addOverride(JsonNode root, String name, String value) {
        JsonNode node = root;
        final Iterator<String> keys = Splitter.on('.').trimResults().split(name).iterator();
        while (keys.hasNext()) {
            final String key = keys.next();
            if (!(node instanceof ObjectNode)) {
                throw new IllegalArgumentException("Unable to override " + name + "; it's not a valid path.");
            }

            final ObjectNode obj = (ObjectNode) node;
            if (keys.hasNext()) {
                JsonNode child = obj.get(key);
                if (child == null) {
                    child = obj.objectNode();
                    obj.put(key, child);
                }
                node = child;
            } else {
                obj.put(key, value);
            }
        }
    }

    private void validate(String file, T config) throws ConfigurationException {
        final ImmutableList<String> errors = validator.validate(config);
        if (!errors.isEmpty()) {
            throw new ConfigurationException(file, errors);
        }
    }
}
