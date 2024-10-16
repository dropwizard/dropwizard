package com.example.app1;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jakarta.rs.json.JacksonXmlBindJsonProvider;
import jakarta.annotation.Priority;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

/** Custom JSON reader and writer that will write a leading HEADER to the JSON output */
@Consumes(MediaType.APPLICATION_JSON)
@Produces(MediaType.APPLICATION_JSON)
@Priority(Priorities.ENTITY_CODER)
public class CustomJsonProvider extends JacksonXmlBindJsonProvider {
    private static final String HEADER = "/** A Dropwizard specialty */\n";
    private static final byte[] HEADER_BYTES = HEADER.getBytes(StandardCharsets.UTF_8);

    public CustomJsonProvider(ObjectMapper mapper) {
        setMapper(mapper);
    }

    @Override
    public void writeTo(
        Object value,
        Class<?> type,
        Type genericType,
        Annotation[] annotations,
        MediaType mediaType,
        MultivaluedMap<String, Object> httpHeaders,
        OutputStream entityStream
    ) throws IOException {
        entityStream.write(HEADER_BYTES);
        super.writeTo(value, type, genericType, annotations, mediaType, httpHeaders, entityStream);
    }

    @Override
    public Object readFrom(
        Class<Object> type,
        Type genericType,
        Annotation[] annotations,
        MediaType mediaType,
        MultivaluedMap<String, String> httpHeaders,
        InputStream entityStream
    ) throws IOException {
        // Attempt to consume our special header from the input so downstream
        // deserializer don't have to deal with the header
        final byte[] ent = new byte[HEADER_BYTES.length];
        final int read = entityStream.read(ent);

        // If our super awesome header is not encountered, you shall not pass
        if (read != ent.length || !Objects.deepEquals(ent, HEADER_BYTES)) {
            throw new WebApplicationException("Super special header not encountered", 400);
        }

        return super.readFrom(type, genericType, annotations, mediaType, httpHeaders, entityStream);
    }
}
