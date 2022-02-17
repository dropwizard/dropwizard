package com.example.app1;

import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

/** Demonstration that one can provider their own message body writers (see issue #1005) */
@Produces(MediaType.APPLICATION_JSON)
public class CustomClassBodyWriter implements MessageBodyWriter<CustomClass> {
    private static final byte[] RESPONSE = "I'm a custom class".getBytes(StandardCharsets.UTF_8);

    @Override
    public boolean isWriteable(Class<?> aClass, Type type, Annotation[] annotations, MediaType mediaType) {
        return true;
    }

    @Override
    public long getSize(
        CustomClass customClass,
        Class<?> aClass,
        Type type,
        Annotation[] annotations,
        MediaType mediaType
    ) {
        return RESPONSE.length;
    }

    @Override
    public void writeTo(
        CustomClass customClass,
        Class<?> aClass,
        Type type,
        Annotation[] annotations,
        MediaType mediaType,
        MultivaluedMap<String, Object> multivaluedMap,
        OutputStream outputStream
    ) throws IOException, WebApplicationException {
        outputStream.write(RESPONSE);
    }
}
