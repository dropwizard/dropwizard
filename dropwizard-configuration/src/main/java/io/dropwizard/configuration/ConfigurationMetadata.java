package io.dropwizard.configuration;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.jsontype.TypeDeserializer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A class to get metadata about the properties that are available in a configuration class. It can
 * be used to get information about the type of the properties. The names are stored as nested paths
 * (e.g. parent.config.field).
 *
 * <p>Given the following simple configuration:</p>
 * <pre>
 * public class ExampleConfiguration extends Configuration {
 *     &#064;NotNull
 *     private String name;
 *
 *     private List&lt;String&gt; names = Collections.emptyList();
 *
 *     &#064;JsonProperty
 *     public String getName() {
 *         return name;
 *     }
 *
 *     &#064;JsonProperty
 *     public List&lt;String&gt; getNames() {
 *         return names;
 *     }
 * }
 * </pre>
 * <p>
 * This leads to the following entries:
 * <ul>
 *     <li><pre>{@code name -> {SimpleType} "[simple type, class java.lang.String]"}</pre></li>
 *     <li><pre>{@code names -> {CollectionType} "[collection type; class java.util.List, contains [simple type, class java.lang.String]]"}</pre></li>
 * </ul>
 * <p>
 * Restrictions: The field-tree is only discovered correctly when no inheritance is present. It is
 * hard to discover the correct class, so this sticks to the defaultImpl that is provided.
 */
public class ConfigurationMetadata extends JsonFormatVisitorWrapper.Base {

    // Just a safety option if someone uses recursive configuration classes
    private static final int MAX_DEPTH = 10;

    private final ObjectMapper mapper;

    // Field is package-private to be visible for unit tests
    final Map<String, JavaType> fields = new HashMap<>();

    private final Set<BeanProperty> parentProps = new HashSet<>();
    private String currentPrefix = "";
    private int currentDepth = 0;

    /**
     * Create a metadata instance and
     *
     * @param mapper the {@link ObjectMapper} that is used to parse the configuration file
     * @param klass  the target class of the configuration
     */
    public ConfigurationMetadata(ObjectMapper mapper, Class<?> klass) {
        this.mapper = mapper;

        try {
            mapper.acceptJsonFormatVisitor(klass, this);
        } catch (JsonMappingException ignored) {
            // empty
        }
    }

    private Optional<JavaType> getTypeOfField(String fieldName) {
        // normalize the field name to recognize arrays correctly
        // (input is field[1].prop but stored as field[*].prop)
        return Optional.ofNullable(fields.get(fieldName.replaceAll("\\[\\d+]", "[*]")));
    }

    /**
     * Check if a field is a collection of strings.
     *
     * @param fieldName the field name
     * @return true, if the field is a collection of strings
     */
    public boolean isCollectionOfStrings(String fieldName) {
        Optional<JavaType> propertyType = getTypeOfField(fieldName);

        if (propertyType.isEmpty()) {
            return false;
        }

        if (!propertyType.get().isCollectionLikeType() && !propertyType.get().isArrayType()) {
            return false;
        }

        return propertyType.get().getContentType().isTypeOrSubTypeOf(String.class);
    }

    @Override
    public JsonObjectFormatVisitor expectObjectFormat(JavaType type) throws JsonMappingException {
        // store the pointer to the own instance
        final ConfigurationMetadata thiss = this;

        return new JsonObjectFormatVisitor.Base() {
            @Override
            public void optionalProperty(BeanProperty prop) throws JsonMappingException {
                // don't run into an infinite loop with circular dependencies
                if (currentDepth >= MAX_DEPTH) {
                    return;
                }

                // check if we already visited the same property
                if (parentProps.contains(prop)) {
                    return;
                }

                if (prop.getAnnotation(JsonIgnore.class) != null) {
                    return;
                }

                // build the complete field path
                String name = !currentPrefix.isEmpty() ? currentPrefix + "." + prop.getName()
                        : prop.getName();

                // set state for the recursive traversal
                int oldFieldSize = fields.size();
                String oldPrefix = currentPrefix;
                currentPrefix = name;
                currentDepth++;

                // the type of the field
                JavaType fieldType = prop.getType();

                // if the field is a collection or array, use the content type instead and add [*]
                // to the path
                if (fieldType.isCollectionLikeType() || fieldType.isArrayType()) {
                    fieldType = fieldType.getContentType();
                    currentPrefix += "[*]";
                }

                // get the type deserializer
                TypeDeserializer typeDeserializer =
                        mapper.getDeserializationConfig().findTypeDeserializer(fieldType);

                // get the default impl if available
                Class<?> defaultImpl =
                        typeDeserializer != null ? typeDeserializer.getDefaultImpl() : null;

                // remember current property
                parentProps.add(prop);

                // visit the type of the property (or its defaultImpl).
                try {
                    mapper.acceptJsonFormatVisitor(defaultImpl == null ? fieldType.getRawClass() : defaultImpl, thiss);
                } catch (NoClassDefFoundError | TypeNotPresentException e) {
                    // this can happen if the default implementation contains
                    // references to classes that are not in the classpath; in
                    // that case, just ignore the default implementation
                    if (defaultImpl != null) {
                        return;
                    } else {
                        // exception has nothing to do with default
                        // implementation, so re-throw it
                        throw e;
                    }
                } finally {
                    // reset state after the recursive traversal
                    parentProps.remove(prop);
                    currentDepth--;
                    currentPrefix = oldPrefix;
                }

                // if no new fields are discovered, we assume that we are at a primitive field
                if (oldFieldSize == fields.size()) {
                    fields.put(name, prop.getType());
                }
            }
        };
    }
}
