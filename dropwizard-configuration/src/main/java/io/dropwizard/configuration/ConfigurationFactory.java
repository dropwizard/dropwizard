package io.dropwizard.configuration;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.error.MarkedYAMLException;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.error.YAMLException;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * A factory class for loading YAML configuration files, binding them to configuration objects, and
 * and validating their constraints. Allows for overriding configuration parameters from system
 * properties.
 *
 * @param <T> the type of the configuration objects to produce
 */
public class ConfigurationFactory<T> {

    private static final Pattern ESCAPED_COMMA_PATTERN = Pattern.compile("\\\\,");
    private static final Splitter ESCAPED_COMMA_SPLITTER = Splitter.on(Pattern.compile("(?<!\\\\),")).trimResults();
    private static final Splitter DOT_SPLITTER = Splitter.on('.').trimResults();

    private final Class<T> klass;
    private final String propertyPrefix;
    private final ObjectMapper mapper;
    private final Validator validator;
    private final YAMLFactory yamlFactory;

    /**
     * Creates a new configuration factory for the given class.
     *
     * @param klass          the configuration class
     * @param validator      the validator to use
     * @param objectMapper   the Jackson {@link ObjectMapper} to use
     * @param propertyPrefix the system property name prefix used by overrides
     */
    public ConfigurationFactory(Class<T> klass,
                                Validator validator,
                                ObjectMapper objectMapper,
                                String propertyPrefix) {
        this.klass = klass;
        this.propertyPrefix = (propertyPrefix == null || propertyPrefix.endsWith("."))
                ? propertyPrefix : (propertyPrefix + '.');
        // Sub-classes may choose to omit data-binding; if so, null ObjectMapper passed:
        if (objectMapper == null) { // sub-class has no need for mapper
            mapper = null;
            yamlFactory = null;
        } else {
            mapper = objectMapper.copy()
                    .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            yamlFactory = new YAMLFactory();
        }
        this.validator = validator;
    }

    /**
     * Loads, parses, binds, and validates a configuration object.
     *
     * @param provider the provider to to use for reading configuration files
     * @param path     the path of the configuration file
     * @return a validated configuration object
     * @throws IOException            if there is an error reading the file
     * @throws ConfigurationException if there is an error parsing or validating the file
     */
    public T build(ConfigurationSourceProvider provider, String path) throws IOException, ConfigurationException {
        try (InputStream input = provider.open(requireNonNull(path))) {
            final JsonNode node = mapper.readTree(yamlFactory.createParser(input));

            if (node == null) {
                throw ConfigurationParsingException
                        .builder("Configuration at " + path + " must not be empty")
                        .build(path);
            }

            return build(node, path);
        } catch (YAMLException e) {
            final ConfigurationParsingException.Builder builder = ConfigurationParsingException
                    .builder("Malformed YAML")
                    .setCause(e)
                    .setDetail(e.getMessage());

            if (e instanceof MarkedYAMLException) {
                builder.setLocation(((MarkedYAMLException) e).getProblemMark());
            }

            throw builder.build(path);
        }
    }

    /**
     * Loads, parses, binds, and validates a configuration object from a file.
     *
     * @param file the path of the configuration file
     * @return a validated configuration object
     * @throws IOException            if there is an error reading the file
     * @throws ConfigurationException if there is an error parsing or validating the file
     */
    public T build(File file) throws IOException, ConfigurationException {
        return build(new FileConfigurationSourceProvider(), file.toString());
    }

    /**
     * Loads, parses, binds, and validates a configuration object from an empty document.
     *
     * @return a validated configuration object
     * @throws IOException            if there is an error reading the file
     * @throws ConfigurationException if there is an error parsing or validating the file
     */
    public T build() throws IOException, ConfigurationException {
        try {
            final JsonNode node = mapper.valueToTree(klass.newInstance());
            return build(node, "default configuration");
        } catch (InstantiationException | IllegalAccessException e) {
            throw new IllegalArgumentException("Unable create an instance " +
                    "of the configuration class: '" + klass.getCanonicalName() + "'", e);
        }
    }

    protected T build(JsonNode node, String path) throws IOException, ConfigurationException {
        for (Map.Entry<Object, Object> pref : System.getProperties().entrySet()) {
            final String prefName = (String) pref.getKey();
            if (prefName.startsWith(propertyPrefix)) {
                final String configName = prefName.substring(propertyPrefix.length());
                addOverride(node, configName, System.getProperty(prefName));
            }
        }

        try {
            final T config = mapper.readValue(new TreeTraversingParser(node), klass);
            validate(path, config);
            return config;
        } catch (UnrecognizedPropertyException e) {
            final List<String> properties = e.getKnownPropertyIds().stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
            throw ConfigurationParsingException.builder("Unrecognized field")
                    .setFieldPath(e.getPath())
                    .setLocation(e.getLocation())
                    .addSuggestions(properties)
                    .setSuggestionBase(e.getPropertyName())
                    .setCause(e)
                    .build(path);
        } catch (InvalidFormatException e) {
            final String sourceType = e.getValue().getClass().getSimpleName();
            final String targetType = e.getTargetType().getSimpleName();
            throw ConfigurationParsingException.builder("Incorrect type of value")
                    .setDetail("is of type: " + sourceType + ", expected: " + targetType)
                    .setLocation(e.getLocation())
                    .setFieldPath(e.getPath())
                    .setCause(e)
                    .build(path);
        } catch (JsonMappingException e) {
            throw ConfigurationParsingException.builder("Failed to parse configuration")
                    .setDetail(e.getMessage())
                    .setFieldPath(e.getPath())
                    .setLocation(e.getLocation())
                    .setCause(e)
                    .build(path);
        }
    }

    protected void addOverride(JsonNode root, String name, String value) {
        JsonNode node = root;
        final String[] parts = Iterables.toArray(DOT_SPLITTER.split(name), String.class);
        for (int i = 0; i < parts.length; i++) {
            final String key = parts[i];

            if (!(node instanceof ObjectNode)) {
                throw new IllegalArgumentException("Unable to override " + name + "; it's not a valid path.");
            }
            final ObjectNode obj = (ObjectNode) node;

            final String remainingPath = Joiner.on('.').join(Arrays.copyOfRange(parts, i, parts.length));
            if (obj.has(remainingPath) && !remainingPath.equals(key)) {
                if (obj.get(remainingPath).isValueNode()) {
                    obj.put(remainingPath, value);
                    return;
                }
            }

            JsonNode child;
            final boolean moreParts = i < parts.length - 1;

            if (key.matches(".+\\[\\d+\\]$")) {
                final int s = key.indexOf('[');
                final int index = Integer.parseInt(key.substring(s + 1, key.length() - 1));
                child = obj.get(key.substring(0, s));
                if (child == null) {
                    throw new IllegalArgumentException("Unable to override " + name +
                            "; node with index not found.");
                }
                if (!child.isArray()) {
                    throw new IllegalArgumentException("Unable to override " + name +
                            "; node with index is not an array.");
                } else if (index >= child.size()) {
                    throw new ArrayIndexOutOfBoundsException("Unable to override " + name +
                            "; index is greater than size of array.");
                }
                if (moreParts) {
                    child = child.get(index);
                    node = child;
                } else {
                    final ArrayNode array = (ArrayNode) child;
                    array.set(index, TextNode.valueOf(value));
                    return;
                }
            } else if (moreParts) {
                child = obj.get(key);
                if (child == null) {
                    child = obj.objectNode();
                    obj.set(key, child);
                }
                if (child.isArray()) {
                    throw new IllegalArgumentException("Unable to override " + name +
                            "; target is an array but no index specified");
                }
                node = child;
            }

            if (!moreParts) {
                if (node.get(key) != null && node.get(key).isArray()) {
                    final ArrayNode arrayNode = (ArrayNode) obj.get(key);
                    arrayNode.removeAll();
                    for (String val : ESCAPED_COMMA_SPLITTER.split(value)) {
                        arrayNode.add(ESCAPED_COMMA_PATTERN.matcher(val).replaceAll(","));
                    }
                } else {
                    obj.put(key, value);
                }
            }
        }
    }

    private void validate(String path, T config) throws ConfigurationValidationException {
        if (validator != null) {
            final Set<ConstraintViolation<T>> violations = validator.validate(config);
            if (!violations.isEmpty()) {
                throw new ConfigurationValidationException(path, violations);
            }
        }
    }
}
