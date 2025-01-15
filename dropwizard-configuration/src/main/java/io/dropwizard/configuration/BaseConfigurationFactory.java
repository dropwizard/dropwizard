package io.dropwizard.configuration;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Objects.requireNonNull;

/**
 * A generic factory class for loading configuration files, binding them to configuration objects, and
 * validating their constraints. Allows for overriding configuration parameters from system properties.
 *
 * @param <T> the type of the configuration objects to produce
 */
public abstract class BaseConfigurationFactory<T> implements ConfigurationFactory<T> {

    private static final Pattern ESCAPED_COMMA_PATTERN = Pattern.compile("\\\\,");
    private static final Pattern ESCAPED_COMMA_SPLIT_PATTERN = Pattern.compile("(?<!\\\\),");
    private static final Pattern ESCAPED_DOT_PATTERN = Pattern.compile("\\\\\\.");
    private static final Pattern ESCAPED_DOT_SPLIT_PATTERN = Pattern.compile("(?<!\\\\)\\.");

    private final Class<T> klass;
    private final String propertyPrefix;
    /**
     * The object mapper to use for mapping configuration files to objects.
     */
    protected final ObjectMapper mapper;
    private final ConfigurationMetadata configurationMetadata;

    @Nullable
    private final Validator validator;

    private final String formatName;
    private final JsonFactory parserFactory;

    /**
     * Creates a new configuration factory for the given class.
     *
     * @param parserFactory  the factory that creates the parser used
     * @param formatName     the name of the format parsed by this factory (used in exceptions)
     * @param klass          the configuration class
     * @param validator      the validator to use
     * @param objectMapper   the object mapper to use
     * @param propertyPrefix the system property name prefix used by overrides
     */
    protected BaseConfigurationFactory(JsonFactory parserFactory,
                                    String formatName,
                                    Class<T> klass,
                                    @Nullable Validator validator,
                                    ObjectMapper objectMapper,
                                    String propertyPrefix) {
        this.klass = klass;
        this.formatName = formatName;
        this.propertyPrefix = propertyPrefix.endsWith(".") ? propertyPrefix : propertyPrefix + '.';
        this.mapper = objectMapper;
        this.parserFactory = parserFactory;
        this.validator = validator;
        this.configurationMetadata = new ConfigurationMetadata(mapper, klass);
    }

    @Override
    public T build(ConfigurationSourceProvider provider, String path) throws IOException, ConfigurationException {
        try (InputStream input = provider.open(requireNonNull(path))) {
            final JsonNode node = mapper.readTree(createParser(input));

            if (node == null) {
                throw ConfigurationParsingException
                    .builder("Configuration at " + path + " must not be empty")
                    .build(path);
            }

            return build(node, path);
        } catch (JsonParseException e) {
            throw ConfigurationParsingException
                .builder("Malformed " + formatName)
                .setCause(e)
                .setLocation(e.getLocation())
                .setDetail(e.getMessage())
                .build(path);
        }
    }

    /**
     * Constructs a {@link JsonParser} to parse the contents of the provided {@link InputStream}.
     *
     * @param input the input to parse
     * @return the JSON parser for the given input
     * @throws IOException if the parser creation fails due to an I/O error
     */
    protected JsonParser createParser(InputStream input) throws IOException {
        return parserFactory.createParser(input);
    }

    @Override
    public T build() throws IOException, ConfigurationException {
        try {
            final T instance = klass.getDeclaredConstructor().newInstance();
            final JsonNode node = mapper.valueToTree(instance);
            return build(node, "default configuration");
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException | SecurityException
                | NoSuchMethodException | InvocationTargetException e) {
            throw new IllegalArgumentException("Unable to create an instance " +
                "of the configuration class: '" + klass.getCanonicalName() + "'", e);
        }
    }

    /**
     * Loads, parses, binds, and validates a configuration object for a given {@link JsonNode}.
     *
     * @param node the json node to parse the configuration from
     * @param path the path of the configuration file
     * @return a validated configuration object
     * @throws IOException if there is an error reading the file
     * @throws ConfigurationException if there is an error parsing or validating the file
     */
    protected T build(JsonNode node, String path) throws IOException, ConfigurationException {
        for (Map.Entry<Object, Object> pref : System.getProperties().entrySet()) {
            final String prefName = (String) pref.getKey();
            if (prefName.startsWith(propertyPrefix)) {
                final String configName = prefName.substring(propertyPrefix.length());
                addOverride(node, configName, System.getProperty(prefName));
            }
        }

        try {
            final T config = mapper.readValue(new TreeTraversingParser(node, mapper), klass);
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

    /**
     * Applies an override to a given {@link JsonNode}.
     *
     * @param root the node to apply the override to
     * @param name the key of the override
     * @param value the new value
     */
    protected void addOverride(JsonNode root, String name, String value) {
        JsonNode node = root;
        final List<String> parts = Arrays.stream(ESCAPED_DOT_SPLIT_PATTERN.split(name))
                .map(String::trim)
                .map(key -> ESCAPED_DOT_PATTERN.matcher(key).replaceAll("."))
                .collect(Collectors.toList());
        for (int i = 0; i < parts.size(); i++) {
            final String key = parts.get(i);

            if (!(node instanceof ObjectNode obj)) {
                throw new IllegalArgumentException("Unable to override " + name + "; it's not a valid path.");
            }

            final String remainingPath = String.join(".", parts.subList(i, parts.size()));
            if (obj.has(remainingPath) && !remainingPath.equals(key)
                    && obj.get(remainingPath).isValueNode()) {
                obj.put(remainingPath, value);
                return;
            }

            JsonNode child;
            final boolean moreParts = i < parts.size() - 1;

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
                if ((node.get(key) != null && node.get(key).isArray())
                    || (node.get(key) == null && configurationMetadata.isCollectionOfStrings(name))) {
                    ArrayNode arrayNode = (ArrayNode) obj.get(key);
                    if (arrayNode == null) {
                        arrayNode = obj.arrayNode();
                        obj.set(key, arrayNode);
                    }
                    arrayNode.removeAll();
                    Arrays.stream(ESCAPED_COMMA_SPLIT_PATTERN.split(value))
                            .map(String::trim)
                            .map(val -> ESCAPED_COMMA_PATTERN.matcher(val).replaceAll(","))
                            .forEach(arrayNode::add);
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
