package com.codahale.dropwizard.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.google.common.base.Splitter;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

// TODO: 4/28/13 <coda> -- document ConfigurationFactory

public class ConfigurationFactory<T> {
    private static final String PROPERTY_PREFIX = "dw.";
    private final Class<T> klass;
    private final ObjectMapper mapper;
    private final Validator validator;

    public ConfigurationFactory(Class<T> klass, Validator validator, ObjectMapper objectMapper) {
        this.klass = klass;
        this.mapper = objectMapper.copy();
        mapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        this.validator = validator;
    }

    public T build(String configurationPath, InputStream inputStream) throws IOException, ConfigurationException {
        final JsonNode node = mapper.readTree(new YAMLFactory().createJsonParser(inputStream));
        return build(node, configurationPath != null ? configurationPath : "InputStream configuration");
    }

    public T build(File file) throws IOException, ConfigurationException {
        try (FileInputStream input = new FileInputStream(file)) {
            return build(file.toString(), input);
        }
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
        final T config = mapper.readValue(new TreeTraversingParser(node), klass);
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
        final Set<ConstraintViolation<T>> violations = validator.validate(config);
        if (!violations.isEmpty()) {
            throw new ConfigurationException(file, violations);
        }
    }
}
