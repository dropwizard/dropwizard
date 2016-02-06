package io.dropwizard.jersey.optional;

import javax.ws.rs.NotFoundException;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.OptionalInt;

@Provider
@Produces(MediaType.WILDCARD)
public class OptionalIntMessageBodyWriter implements MessageBodyWriter<OptionalInt> {
    // Jersey ignores this
    @Override
    public long getSize(OptionalInt entity, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return (OptionalInt.class.isAssignableFrom(type));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void writeTo(OptionalInt entity,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException {
        if (!entity.isPresent()) {
            throw new NotFoundException();
        }

        entityStream.write(Integer.toString(entity.getAsInt()).getBytes(StandardCharsets.US_ASCII));
    }
}
