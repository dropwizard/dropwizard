package io.dropwizard.jetty;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;
import java.util.function.Function;

class StringMethodSerializer<T> extends StdSerializer<T> {

    private final Function<T, String> serializerFunction;

    protected StringMethodSerializer(Class<T> clazz, Function<T, String> serializerFunction) {
        super(clazz);
        this.serializerFunction = serializerFunction;
    }

    @Override
    public void serialize(T t, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (t == null) {
            jsonGenerator.writeNull();
        } else {
            jsonGenerator.writeString(serializerFunction.apply(t));
        }
    }
}
