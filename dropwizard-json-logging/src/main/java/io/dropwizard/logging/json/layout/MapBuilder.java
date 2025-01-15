package io.dropwizard.logging.json.layout;

import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Supplier;

import static java.util.Objects.requireNonNull;

/**
 * Builds a Java map based on the provided configuration and customization.
 */
public class MapBuilder {

    private final TimestampFormatter timestampFormatter;

    /**
     * Custom field name replacements in the format (oldName:newName).
     */
    private final Map<String, String> customFieldNames;

    /**
     * Additional fields which should be included in the message.
     */
    private final Map<String, Object> additionalFields;

    private final Map<String, Object> map;

    public MapBuilder(TimestampFormatter timestampFormatter, Map<String, String> customFieldNames,
                      Map<String, Object> additionalFields, int expectedSize) {
        this.timestampFormatter = timestampFormatter;
        this.customFieldNames = requireNonNull(customFieldNames);
        this.additionalFields = requireNonNull(additionalFields);
        this.map = new LinkedHashMap<>(expectedSize);
        this.map.put(getFieldName("timestamp"), null); // Insert a null timestamp at first and fix it at build time
    }

    /**
     * Adds the string value to the provided map under the provided field name,
     * if it should be included.
     */
    public MapBuilder add(String fieldName, boolean include, @Nullable String value) {
        if (include && value != null) {
            map.put(getFieldName(fieldName), value);
        }
        return this;
    }

    /**
     * Adds the string value to the provided map under the provided field name,
     * if it should be included. The supplier is only invoked if the field is to be included.
     *
     * @since 2.0
     */
    public MapBuilder add(String fieldName, boolean include, Supplier<String> supplier) {
        if (include) {
            String value = supplier.get();
            if (value != null) {
                map.put(getFieldName(fieldName), value);
            }
        }
        return this;
    }

    /**
     * Adds the number to the provided map under the provided field name if it's should be included.
     */
    public MapBuilder addNumber(String fieldName, boolean include, @Nullable Number number) {
        if (include && number != null) {
            map.put(getFieldName(fieldName), number);
        }
        return this;
    }

    /**
     * Adds the number value to the provided map under the provided field name,
     * if it should be included. The supplier is only invoked if the field is to be included.
     *
     * @since 2.0
     */
    public MapBuilder addNumber(String fieldName, boolean include, Supplier<Number> supplier) {
        if (include) {
            Number value = supplier.get();
            if (value != null) {
                map.put(getFieldName(fieldName), value);
            }
        }
        return this;
    }

    /**
     * Adds the map to the provided map under the provided field name if it's should be included.
     */
    public MapBuilder add(String fieldName, boolean include, @Nullable Map<String, ?> mapValue) {
        if (include && mapValue != null && !mapValue.isEmpty()) {
            map.put(getFieldName(fieldName), mapValue);
        }
        return this;
    }

    /**
     * Adds the map value to the provided map under the provided field name, if it should be
     * included. The supplier is only invoked if the field is to be included.
     *
     * @since 2.0
     */
    public MapBuilder addMap(String fieldName, boolean include, Supplier<Map<String, ?>> supplier) {
        if (include) {
            Map<String, ?> value = supplier.get();
            if (value != null && !value.isEmpty()) {
                map.put(getFieldName(fieldName), value);
            }
        }
        return this;
    }


    /**
     * Adds and optionally formats the timestamp to the provided map under the provided field name,
     * if it's should be included.
     */
    public MapBuilder addTimestamp(String fieldName, boolean include, long timestamp) {
        if (include && timestamp > 0) {
            map.put(getFieldName(fieldName), timestampFormatter.format(timestamp));
        }
        return this;
    }

    private String getFieldName(String fieldName) {
        return customFieldNames.getOrDefault(fieldName, fieldName);
    }

    public Map<String, Object> build() {
        map.putAll(additionalFields);
        map.remove(getFieldName("timestamp"), null); // drop the "timestamp" field if never assigned a real value
        return map;
    }

}
