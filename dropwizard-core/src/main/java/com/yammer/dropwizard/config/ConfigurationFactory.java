package com.yammer.dropwizard.config;

import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableList;
import com.yammer.dropwizard.json.Json;
import com.yammer.dropwizard.validation.Validator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.Module;
import org.codehaus.jackson.node.ObjectNode;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

public class ConfigurationFactory<T> {

    private static final String PROPERTY_PREFIX = "dw.";
    private static final String CONFIG_LOCATION = System.getProperty("user.home") + "/.dw/";

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
        this.json = new Json();
        json.enable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);
        for (Module module : modules) {
            json.registerModule(module);
        }
        this.validator = validator;
    }
    
    public T build(File file) throws IOException, ConfigurationException {
        final JsonNode node = parse(file);
        final String appName = file.getName();
        
        merge(node, CONFIG_LOCATION + appName);     // contains app specific config maintained by support
        merge(node, CONFIG_LOCATION + "shared.yml"); // contains shared config values, e.g. common db, paths
        
        for (Map.Entry<Object, Object> pref : System.getProperties().entrySet()) {
            final String prefName = (String) pref.getKey();
            if (prefName.startsWith(PROPERTY_PREFIX)) {
                final String configName = prefName.substring(PROPERTY_PREFIX.length());
                addOverride(node, configName, System.getProperty(prefName));
            }
        }
        final T config = json.readValue(node, klass);
        validate(file, config);
        return config;
    }
    
    private void merge(JsonNode node, String fileLocation) throws IOException {
        File config = new File(fileLocation);
    	if (config.exists()) {
    		final JsonNode override = parse(config);
    		
    		merge((ObjectNode) node, (ObjectNode) override);
    	}
    }
    
	private static void merge(ObjectNode n1, ObjectNode n2) {
		Iterator<Map.Entry<String, JsonNode>> itr = n2.getFields();

		while (itr.hasNext()) {
			Map.Entry<String, JsonNode> n2ChildEntry = itr.next();

			String childKey = n2ChildEntry.getKey();
			JsonNode n1Child = n1.get(childKey);
			JsonNode n2Child = n2ChildEntry.getValue();

			if (n1Child == null) {
				n1.put(childKey, n2Child);
			} else {
				if (n2Child instanceof ObjectNode) {
					merge((ObjectNode) n1.get(childKey), (ObjectNode) n2Child);
				} else {
					n1.put(childKey, n2Child);
				}
			}
		}
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

    private JsonNode parse(File file) throws IOException {
        if (file.getName().endsWith(".yaml") || file.getName().endsWith(".yml")) {
            return json.readYamlValue(file, JsonNode.class);
        }
        return json.readValue(file, JsonNode.class);
    }

    private void validate(File file, T config) throws ConfigurationException {
        final ImmutableList<String> errors = validator.validate(config);
        if (!errors.isEmpty()) {
            throw new ConfigurationException(file, errors);
        }
    }
}
