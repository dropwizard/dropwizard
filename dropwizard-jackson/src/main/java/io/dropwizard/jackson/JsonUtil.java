package io.dropwizard.jackson;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.Iterator;

public class JsonUtil {

    private JsonUtil() {
        // Static helper class
    }

    public static JsonNode merge(JsonNode extendedNode, JsonNode mergedNode) {
        Iterator<String> fieldNames = mergedNode.fieldNames();
        while (fieldNames.hasNext()) {
            String fieldName = fieldNames.next();
            JsonNode jsonNode = extendedNode.get(fieldName);

            // if field exists and is an embedded object
            if (jsonNode != null && jsonNode.isObject()) {
                merge(jsonNode, mergedNode.get(fieldName));
            } else {
                if (extendedNode instanceof ObjectNode) {
                    // Overwrite field
                    JsonNode value = mergedNode.get(fieldName);
                    ((ObjectNode) extendedNode).replace(fieldName, value);
                }
            }

        }

        return extendedNode;
    }
}
