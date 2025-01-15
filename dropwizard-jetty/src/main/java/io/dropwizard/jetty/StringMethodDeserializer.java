package io.dropwizard.jetty;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import org.jspecify.annotations.Nullable;

import java.io.IOException;
import java.util.function.Function;

class StringMethodDeserializer<T> extends StdDeserializer<T> {

    private final Function<String, T> deserializerFunction;

    protected StringMethodDeserializer(Class<T> clazz, Function<String, T> deserializerFunction) {
        super(clazz);
        this.deserializerFunction = deserializerFunction;
    }

    @Override
    public @Nullable T deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        String text = jsonParser.getText();
        if (text != null && !text.isEmpty()) {
            return deserializerFunction.apply(text);
        }
        return null;
    }
}
