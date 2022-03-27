package io.dropwizard.jersey.optional;

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import jakarta.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.OptionalLong;

import static java.nio.charset.StandardCharsets.US_ASCII;

@Provider
@Produces(MediaType.WILDCARD)
public class OptionalLongMessageBodyWriter implements MessageBodyWriter<OptionalLong> {
    // Jersey ignores this
    @Override
    public long getSize(OptionalLong entity, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return -1;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return OptionalLong.class.isAssignableFrom(type);
    }

    @Override
    public void writeTo(OptionalLong entity,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException {
        final String body = Long.toString(entity.orElseThrow(() -> EmptyOptionalException.INSTANCE));
        entityStream.write(body.getBytes(US_ASCII));
    }
}
