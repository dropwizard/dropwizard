package io.dropwizard.logging.json.layout;

import ch.qos.logback.core.CoreConstants;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import javax.annotation.Nullable;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

/**
 * Formats objects to JSON strings according to the configured {@link ObjectMapper} and output parameters.
 */
public class JsonFormatter {

    private static final int DEFAULT_BUFFER_SIZE = 512;

    private final ObjectMapper objectMapper;
    private final boolean doesAppendLineSeparator;
    private final int bufferSize;

    public JsonFormatter(ObjectMapper objectMapper, boolean prettyPrint, boolean doesAppendLineSeparator,
                         int bufferSize) {
        this.objectMapper = prettyPrint ? objectMapper.enable(SerializationFeature.INDENT_OUTPUT) : objectMapper;
        this.doesAppendLineSeparator = doesAppendLineSeparator;
        this.bufferSize = bufferSize;
    }

    public JsonFormatter(ObjectMapper objectMapper, boolean prettyPrint, boolean doesAppendLineSeparator) {
        this(objectMapper, prettyPrint, doesAppendLineSeparator, DEFAULT_BUFFER_SIZE);
    }

    /**
     * Converts the provided map as a JSON object according to the configured JSON mapper.
     *
     * @param map the provided map
     * @return the JSON as a string
     */
    @Nullable
    public String toJson(Map<String, Object> map) {
        if (map == null || map.isEmpty()) {
            return null;
        }

        final StringWriter writer = new StringWriter(bufferSize);
        try {
            objectMapper.writeValue(writer, map);
        } catch (IOException e) {
            throw new IllegalArgumentException("Unable to format map as a JSON", e);
        }
        if (doesAppendLineSeparator) {
            writer.append(CoreConstants.LINE_SEPARATOR);
        }
        return writer.toString();
    }
}
