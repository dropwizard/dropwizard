package com.example.app1;

import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;

/** Demonstration that one can provider their own message body writers (see issue #1005) */
@Produces(MediaType.APPLICATION_JSON)
public class CustomClassBodyWriter implements MessageBodyWriter<CustomClass> {
    private final static byte[] RESPONSE = "I'm a custom class".getBytes(StandardCharsets.UTF_8);

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
