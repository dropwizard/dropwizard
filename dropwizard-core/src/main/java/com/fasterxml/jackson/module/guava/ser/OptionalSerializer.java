package com.fasterxml.jackson.module.guava.ser;

import com.google.common.base.Optional;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.JsonSerializer;
import org.codehaus.jackson.map.SerializerProvider;
import org.codehaus.jackson.map.annotate.JsonCachable;

import java.io.IOException;

@JsonCachable
public class OptionalSerializer<T> extends JsonSerializer<Optional<T>> {
    @Override
    public void serialize(Optional<T> value,
                          JsonGenerator jgen,
                          SerializerProvider provider) throws IOException {
        if (value.isPresent()) {
            jgen.writeObject(value.get());
        } else {
            jgen.writeNull();
        }
    }
}
