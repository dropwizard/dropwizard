package io.dropwizard.jersey.jackson;

import com.fasterxml.jackson.annotation.JsonIgnoreType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.jaxrs.json.JacksonJaxbJsonProvider;

import javax.ws.rs.core.MediaType;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * A Jersey provider which enables using Jackson to parse request entities into objects and generate
 * response entities from objects.
 * <p/>
 * (Essentially, extends {@link JacksonJaxbJsonProvider} with support for {@link JsonIgnoreType}.)
 */
public class JacksonMessageBodyProvider extends JacksonJaxbJsonProvider {
    private final ObjectMapper mapper;

    public JacksonMessageBodyProvider(ObjectMapper mapper) {
        this.mapper = mapper;
        setMapper(mapper);
    }

    @Override
    public boolean isReadable(Class<?> type,
                              Type genericType,
                              Annotation[] annotations,
                              MediaType mediaType) {
        return isProvidable(type) && super.isReadable(type, genericType, annotations, mediaType);
    }

    @Override
    public boolean isWriteable(Class<?> type,
                               Type genericType,
                               Annotation[] annotations,
                               MediaType mediaType) {
        return isProvidable(type) && super.isWriteable(type, genericType, annotations, mediaType);
    }

    private boolean isProvidable(Class<?> type) {
        final JsonIgnoreType ignore = type.getAnnotation(JsonIgnoreType.class);
        return (ignore == null) || !ignore.value();
    }

    public ObjectMapper getObjectMapper() {
        return mapper;
    }
}
