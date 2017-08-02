package io.dropwizard.jersey.jackson;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

import java.io.IOException;

/**
 * A custom deserializer of your nightmares. It tries to get {@link JsonProcessingExceptionMapper}
 * to fail due to a null message. Jackson does a great job of preventing accidental null
 * propagation, but it won't stop someone who'll do anything to get an unexpected null pointer
 * exception somewhere.
 */
public class CustomDeserialization extends StdDeserializer<CustomRepresentation> {
    public CustomDeserialization() {
        super((Class) null);
    }

    @Override
    public CustomRepresentation deserialize(
        JsonParser jsonParser,
        DeserializationContext deserializationContext
    ) throws IOException {
        final Object ss = jsonParser.readValueAs(Object.class);
        if (ss.equals("SQL_INECTION")) {
            throw new RuntimeException("Database fell over due to sql injection");
        } else {
            throw new MyNastyException(jsonParser);
        }
    }

    /**
     * We can't get a regular {@link JsonMappingException} to report a null message, so we'll derive
     * our own for our devious plan.
     */
    public static class MyNastyException extends JsonMappingException {
        public MyNastyException(JsonParser jp) {
            super(jp::close, null);
        }

        @Override
        public String getMessage() {
            return null;
        }
    }
}
