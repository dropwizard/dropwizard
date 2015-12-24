package io.dropwizard;

import static com.google.common.base.Preconditions.checkNotNull;
import io.dropwizard.configuration.ConfigurationException;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.configuration.ConfigurationSourceProvider;
import io.dropwizard.configuration.ConfigurationValidationException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.Yaml;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.error.MarkedYAMLException;
import com.fasterxml.jackson.dataformat.yaml.snakeyaml.error.YAMLException;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Iterables;

/**
 * A factory to load configuration files with inheritance. In JSON config files, parent files are defined in the
 * parentConfigurationFile field at the top level.
 *
 * @author JAshe
 * @param <T>
 */
public class MergedConfigurationFactory<T> extends ConfigurationFactory<T> {

    /**
     * The logger for this class
     */
    private static final Logger LOG = LoggerFactory.getLogger(MergedConfigurationFactory.class);

    private static final String PARENT_FILE_FIELD_NAME = "parentConfigurationFile";
    private static final String PARENT_URL_FIELD_NAME = "parentConfigurationURL";
    private final Class<T> klass;
    private final String propertyPrefix;
    private final ObjectMapper mapper;
    private final Validator validator;
    private final YAMLFactory yamlFactory;
    /**
     * THE regex to search a string to see if it contains ${myVariable}
     */
    private static final String VARIABLE_TEMPLATE = ".*\\$\\{.+\\}.*";

    /**
     * Creates a new configuration factory for the given class.
     *
     * @param klass the configuration class
     * @param validator the validator to use
     * @param objectMapper the Jackson {@link ObjectMapper} to use
     * @param propertyPrefix the system property name prefix used by overrides
     */
    public MergedConfigurationFactory(Class<T> klass,
            Validator validator,
            ObjectMapper objectMapper,
            String propertyPrefix) {
        super(klass, validator, objectMapper, propertyPrefix);
        this.klass = klass;
        this.propertyPrefix = propertyPrefix.endsWith(".") ? propertyPrefix : propertyPrefix + '.';
        this.mapper = objectMapper.copy();
        mapper.enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        this.validator = validator;
        this.yamlFactory = new YAMLFactory();
    }

    /**
     * Calls the appropriate version of build using a MultiConfigurationSourceProvider.
     *
     * @param provider the provider to to use for reading configuration files
     * @param path the path of the configuration file
     * @return a validated configuration object
     * @throws IOException if there is an error reading the file
     * @throws ConfigurationSourceException if the method is called without a MultiSourceConfigurationProvider
     */
    @Override
    public T build(ConfigurationSourceProvider provider, String path) throws IOException, ConfigurationException {
        if (provider instanceof MultiConfigurationSourceProvider) {
            return build((MultiConfigurationSourceProvider) provider, path);
        } else {
            throw new ConfigurationSourceException();
        }

    }

    /**
     * Loads, parses, binds, and validates a configuration object. Iteratively finds parent files and loads them,
     * overwriting fields of parent files with the lowest level of file
     *
     * @param provider the MultiConfigurationSourceProvider to to use for reading configuration files
     * @param path the path of the configuration file
     * @return a validated configuration object
     * @throws IOException if there is an error reading the file
     * @throws ConfigurationException if there is an error parsing or validating the file
     */
    public T build(MultiConfigurationSourceProvider provider, String path) throws IOException, ConfigurationException {
        boolean moreParents = true;
        boolean nextIsURL = false;
        String nextFilePath = path;
        ArrayList<JsonNode> nodeList = new ArrayList<>();
        ArrayList<String> fileList = new ArrayList<>();
        // Iterate through JSON files, finding parents and putting each file into a node
        while (moreParents) {
            try (InputStream fileInput = provider.open(checkNotNull(nextFilePath), nextIsURL)) {

                // Exposing SnakeYAML implementation to support merging
                // https://github.com/FasterXML/jackson-dataformat-yaml/issues/20
                Yaml yaml = new Yaml();
                Map<?, ?> normalized = (Map<?, ?>) yaml.load(fileInput);
                String fixed = mapper.writeValueAsString(normalized);
                
                JsonNode headNode = mapper.readTree(fixed);
                JsonNode parentFileNode = headNode.path(PARENT_FILE_FIELD_NAME);
                JsonNode parentURLNode = headNode.path(PARENT_URL_FIELD_NAME);

                // If a circular dependency exists, attempt to build as-is.
                // If a complete set of config information exists, the build
                // will work normally. If not, a ConfigurationException will
                // be thrown
                if (fileList.contains(nextFilePath)) {
                    return build(nodeList, path);
                }

                // Insert nodes at beginning, pushing nodes back. thus the
                // highest-level node will be first, and the lowest last
                nodeList.add(0, headNode);
                fileList.add(nextFilePath);

                // If a config URL is present, use that. 
                // Otherwise, look for a local config file. 
                // Otherwise, build the config.
                if (!parentURLNode.isMissingNode()) {
                    nextFilePath = headNode.path(PARENT_URL_FIELD_NAME).asText();
                    nextIsURL = true;
                } else if (!parentFileNode.isMissingNode()) {
                    nextFilePath = headNode.path(PARENT_FILE_FIELD_NAME).asText();
                    nextIsURL = false;
                } else {
                    return build(nodeList, path);
                }
            } catch (YAMLException e) {
                MergedConfigurationParsingException.Builder builder = MergedConfigurationParsingException
                        .builder("Malformed YAML")
                        .setCause(e)
                        .setDetail(e.getMessage());

                if (e instanceof MarkedYAMLException) {
                    builder.setLocation(((MarkedYAMLException) e).getProblemMark());
                }

                throw builder.build(path);
            }
        }
        // This return statement will never be reached
        return null;
    }

    // Copied directly from Dropwizard implementation
    @Override
    protected T build(JsonNode node, String path) throws IOException, ConfigurationException {
        for (Map.Entry<Object, Object> pref : System.getProperties().entrySet()) {
            final String prefName = (String) pref.getKey();
            if (prefName.startsWith(propertyPrefix)) {
                final String configName = prefName.substring(propertyPrefix.length());
                addOverride(node, configName, System.getProperty(prefName));
            }
        }
        // This is where I should do variable search
        findAndReplaceVars((ObjectNode) node);
        try {
            final T config = mapper.readValue(new TreeTraversingParser(node), klass);
            validate(path, config);
            return config;
        } catch (UnrecognizedPropertyException e) {
            Collection<Object> knownProperties = e.getKnownPropertyIds();
            List<String> properties = new ArrayList<>(knownProperties.size());
            for (Object property : knownProperties) {
                properties.add(property.toString());
            }
            throw MergedConfigurationParsingException.builder("Unrecognized field")
                    .setFieldPath(e.getPath())
                    .setLocation(e.getLocation())
                    .addSuggestions(properties)
                    .setSuggestionBase(e.getPropertyName())
                    .setCause(e)
                    .build(path);
        } catch (InvalidFormatException e) {
            String sourceType = e.getValue().getClass().getSimpleName();
            String targetType = e.getTargetType().getSimpleName();
            throw MergedConfigurationParsingException.builder("Incorrect type of value")
                    .setDetail("is of type: " + sourceType + ", expected: " + targetType)
                    .setLocation(e.getLocation())
                    .setFieldPath(e.getPath())
                    .setCause(e)
                    .build(path);
        } catch (JsonMappingException e) {
            throw MergedConfigurationParsingException.builder("Failed to parse configuration")
                    .setDetail(e.getMessage())
                    .setFieldPath(e.getPath())
                    .setLocation(e.getLocation())
                    .setCause(e)
                    .build(path);
        }
    }

    // Copied directly from Dropwizard implementation
    @Override
    protected void addOverride(JsonNode root, String name, String value) {
        JsonNode node = root;
        final Iterable<String> split = Splitter.on('.').trimResults().split(name);
        final String[] parts = Iterables.toArray(split, String.class);

        for (int i = 0; i < parts.length; i++) {
            String key = parts[i];

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
                key = key.substring(0, s);
                child = obj.get(key);
                if (child == null) {
                    throw new IllegalArgumentException("Unable to override " + name
                            + "; node with index not found.");
                }
                if (!child.isArray()) {
                    throw new IllegalArgumentException("Unable to override " + name
                            + "; node with index is not an array.");
                } else if (index >= child.size()) {
                    throw new ArrayIndexOutOfBoundsException("Unable to override "
                            + name + "; index is greater than size of array.");
                }
                if (moreParts) {
                    child = child.get(index);
                    node = child;
                } else {
                    ArrayNode array = (ArrayNode) child;
                    array.set(index, TextNode.valueOf(value));
                    return;
                }
            } else if (moreParts) {
                child = obj.get(key);
                if (child == null) {
                    child = obj.objectNode();
                    obj.put(key, child);
                }
                if (child.isArray()) {
                    throw new IllegalArgumentException("Unable to override " + name
                            + "; target is an array but no index specified");
                }
                node = child;
            }

            if (!moreParts) {
                if (node.get(key) != null && node.get(key).isArray()) {
                    ArrayNode arrayNode = (ArrayNode) obj.get(key);
                    arrayNode.removeAll();
                    Pattern escapedComma = Pattern.compile("\\\\,");
                    for (String val : Splitter.on(Pattern.compile("(?<!\\\\),")).trimResults().split(value)) {
                        arrayNode.add(escapedComma.matcher(val).replaceAll(","));
                    }
                } else {
                    obj.put(key, value);
                }
            }
        }
    }

    /**
     * Deep merge of mainNode and updateNode. A single node that is a combination of the two is returned, and each
     * sub-node is looked at to see if the value needs to be added or replaced. Source:
     * http://stackoverflow.com/questions/9895041/merging-two-json-documents-using-jackson
     *
     * @param mainNode node that will be overwritten
     * @param updateNode node that contains fields to overwrite with
     * @return a merged node
     */
    public static JsonNode merge(JsonNode mainNode, JsonNode updateNode) {
        Iterator<String> fieldNames = updateNode.fieldNames();
        while (fieldNames.hasNext()) {

            String fieldName = fieldNames.next();
            JsonNode jsonNode = mainNode.get(fieldName);
            // if field exists and is an embedded object
            if (jsonNode != null && jsonNode.isObject()) {
                merge(jsonNode, updateNode.get(fieldName));
            } else {
                if (mainNode instanceof ObjectNode) {
                    // Overwrite field
                    JsonNode value = updateNode.get(fieldName);
                    ((ObjectNode) mainNode).put(fieldName, value);
                }
            }
        }
        return mainNode;
    }

    // Iterates through file list 
    private T build(ArrayList<JsonNode> nodeList, String path) throws IOException, ConfigurationException {
        JsonNode result = nodeList.get(0);
        for (int i = 1; i < nodeList.size(); i++) {
            result = merge(result, nodeList.get(i));
        }
        return build(result, path);
    }

    // Copied directly from Dropwizard implementation
    private void validate(String path, T config) throws ConfigurationValidationException {
        final Set<ConstraintViolation<T>> violations = validator.validate(config);
        if (!violations.isEmpty()) {
            throw new ConfigurationValidationException(path, violations);
        }
    }

    /**
     * Go find all the variables to replace (ie. ${myVariable}) and then replace them in the merged json configuration
     *
     * @param root The root node to traverse
     * @throws IOException Thrown if there's issues reading through the config
     */
    private void findAndReplaceVars(ObjectNode root) throws IOException {
        //First traverse the nodes to find all the values that we can use for resolving the variables
        Map<String, JsonNode> values = findValues(null, root);

        //Next replace all of the variables with one of the values provided
        findAndReplaceVars(root, values);
    }

    /**
     * First, make a map of all top-level value nodes (that can be used as variable values)
     *
     * @param parent The parent key used to create key structure of "myvariable.nestedVariable"
     * @param root check everything below this node
     * @return A Map of key => JsonNode values
     */
    private Map<String, JsonNode> findValues(String parent, ObjectNode root) throws
            IOException {
        if (parent == null) {
            parent = "";
        } else {
            parent = String.format("%s.", parent);
        }

        // Value node pairs that can be used as replacement at this level and below
        Map<String, JsonNode> valueNodePairs = new HashMap<>();
        // Nodes that need to be recursed/explored through for replacement
        Map<String, ObjectNode> toBeTraversedNodes = new HashMap<>();
        // array Nodes that need to be recursed/explored
        Map<String, ArrayNode> arrayNodesToBeTraversed = new HashMap<>();

        // Find value nodes (that can be used for replacement) and object nodes (that may have sub nodes subject to 
        //  replacement)
        Iterator<Map.Entry<String, JsonNode>> children = root.fields();
        while (children.hasNext()) {
            Map.Entry<String, JsonNode> next = children.next();
            String key = String.format("%s%s", parent, next.getKey());
            if (next.getValue().isObject()) {
                toBeTraversedNodes.put(key, (ObjectNode) next.getValue());
            } else if (next.getValue().isArray()) {
                arrayNodesToBeTraversed.put(key, (ArrayNode) next.getValue());
            } else {
                valueNodePairs.put(key, next.getValue());
            }
        }

        // Go through all of the nodes in the array
        for (Map.Entry<String, ArrayNode> nodeToBeReplaced : arrayNodesToBeTraversed.entrySet()) {
            int index = 0;
            for (JsonNode jsonNode : nodeToBeReplaced.getValue()) {
                String key = String.format("%s.%s", nodeToBeReplaced.getKey(), index++);
                if (jsonNode.isObject()) {
                    toBeTraversedNodes.put(key, (ObjectNode) jsonNode);
                } else {
                    valueNodePairs.put(key, jsonNode);
                }
            }
        }

        // Recurse through lower-level nodes
        for (Map.Entry<String, ObjectNode> nodeToBeTraversed : toBeTraversedNodes.entrySet()) {
            valueNodePairs.putAll(findValues(nodeToBeTraversed.getKey(), nodeToBeTraversed.getValue()));
        }

        return valueNodePairs;
    }

    /**
     * First, make a map of all top-level value nodes (that can be used as variable values) Then, recurse through to
     * find all value nodes with a variable name in their key and make the replacement.
     *
     * @param root check everything below this node
     * @param values a Map of TextNodes that can be used for replacement
     */
    private void findAndReplaceVars(ObjectNode root, Map<String, JsonNode> values) throws
            IOException {
        // Nodes that have at least one variable name in them
        Map<String, JsonNode> toBeReplacedNodes = new HashMap<>();
        // Nodes that need to be recursed/explored through for replacement
        Map<String, ObjectNode> toBeTraversedNodes = new HashMap<>();
        // Nodes that need to be replaced and are of type Array
        Map<String, ArrayNode> arrayNodesToBeTraversed = new HashMap<>();

        // Find value nodes (that can be used for replacement) and object nodes (that may have sub nodes subject to 
        //  replacement)
        Iterator<Map.Entry<String, JsonNode>> children = root.fields();
        while (children.hasNext()) {
            Map.Entry<String, JsonNode> next = children.next();
            String key = next.getKey();
            if (next.getValue().isObject()) {
                toBeTraversedNodes.put(key, (ObjectNode) next.getValue());
            } else if (next.getValue().isArray()) {
                arrayNodesToBeTraversed.put(key, (ArrayNode) next.getValue());
            } else {
                //this matches any key that has at least one ${variable}
                if (next.getValue().asText().matches(VARIABLE_TEMPLATE)) {
                    toBeReplacedNodes.put(key, next.getValue());
                }
            }
        }

        // Go through all of the nodes in the array
        for (Map.Entry<String, ArrayNode> nodeToBeReplaced : arrayNodesToBeTraversed.entrySet()) {
            ArrayNode newArray = new ArrayNode(JsonNodeFactory.instance);

            for (JsonNode jsonNode : nodeToBeReplaced.getValue()) {
                if (jsonNode.isObject()) {
                    findAndReplaceVars((ObjectNode) jsonNode, values);
                    newArray.add(jsonNode);
                } else {
                    //this matches any key that has at least one ${variable}
                    if (jsonNode.asText().matches(VARIABLE_TEMPLATE)) {
                        updateArrayNode(jsonNode, values, newArray);
                    } else {
                        newArray.add(jsonNode);
                    }
                }
            }
            root.put(nodeToBeReplaced.getKey(), newArray);
        }

        // Recurse through lower-level nodes
        for (Map.Entry<String, ObjectNode> nodeToBeTraversed : toBeTraversedNodes.entrySet()) {
            findAndReplaceVars(nodeToBeTraversed.getValue(), values);
        }

        // Perform Replacement
        for (Map.Entry<String, JsonNode> nodeToBeReplaced : toBeReplacedNodes.entrySet()) {
            updateObjectNode(nodeToBeReplaced, values, root);
        }
    }

    private void updateArrayNode(JsonNode jsonNode, Map<String, JsonNode> values, ArrayNode newArray) {
        String nodeText = replace(jsonNode, values);

        //Try to replace the value back to the value type that you want boolean/string/number
        if (nodeText.equalsIgnoreCase(Boolean.TRUE.toString()) || nodeText.
                equalsIgnoreCase(Boolean.FALSE.toString())) {
            newArray.add(Boolean.valueOf(nodeText));
        } else if (NumberUtils.isNumber(nodeText)) {
            Number num = NumberUtils.createNumber(nodeText);
            double preciseValue = num.doubleValue();
            if (preciseValue == Math.rint(preciseValue)) {
                newArray.add(Long.parseLong(nodeText));
            } else {
                newArray.add(Double.parseDouble(nodeText));
            }
        } else {
            newArray.add(nodeText);
        }
    }

    private void updateObjectNode(Map.Entry<String, JsonNode> nodeToBeReplaced, Map<String, JsonNode> values,
            ObjectNode root) {

        String nodeText = replace(nodeToBeReplaced.getValue(), values);

        //Try to replace the value back to the value type that you want boolean/string/number
        if (nodeText.equalsIgnoreCase(Boolean.TRUE.toString()) || nodeText.
                equalsIgnoreCase(Boolean.FALSE.toString())) {
            root.put(nodeToBeReplaced.getKey(), Boolean.valueOf(nodeText));
        } else if (NumberUtils.isNumber(nodeText)) {
            Number num = NumberUtils.createNumber(nodeText);
            double preciseValue = num.doubleValue();
            if (preciseValue == Math.rint(preciseValue)) {
                root.put(nodeToBeReplaced.getKey(), Long.parseLong(nodeText));
            } else {
                root.put(nodeToBeReplaced.getKey(), Double.parseDouble(nodeText));
            }
        } else {
            root.put(nodeToBeReplaced.getKey(), nodeText);
        }
    }

    private String replace(JsonNode nodeToBeReplaced, Map<String, JsonNode> values) {
        String nodeText = nodeToBeReplaced.asText();
        for (String keyName : StringUtils.substringsBetween(nodeText, "${", "}")) {
            if (values.containsKey(keyName)) {
                String replacementValue = values.get(keyName).asText();
                nodeText = nodeText.replaceAll("\\$\\{" + keyName + "\\}", replacementValue);
            } else {
                throw new RuntimeException("A replacement value was not found for " + keyName);
            }
        }
        return nodeText;
    }
}
