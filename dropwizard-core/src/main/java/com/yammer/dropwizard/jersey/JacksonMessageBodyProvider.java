package com.yammer.dropwizard.jersey;

import com.google.common.collect.ImmutableList;
import com.yammer.dropwizard.json.Json;
import com.yammer.dropwizard.logging.Log;
import com.yammer.dropwizard.validation.InvalidEntityException;
import com.yammer.dropwizard.validation.Validator;
import org.codehaus.jackson.JsonProcessingException;
import org.eclipse.jetty.io.EofException;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * A Jersey provider which enables using Jackson to parse request entities into objects and generate
 * response entities from objects. Any request entity method parameters annotated with
 * {@code @Valid} are validated, and an informative 422 Unprocessable Entity response is returned
 * should the entity be invalid.
 */
@Provider
@Produces("application/json")
@Consumes("application/json")
public class JacksonMessageBodyProvider implements MessageBodyReader<Object>,
                                                   MessageBodyWriter<Object> {
    private static final Log LOG = Log.forClass(JacksonMessageBodyProvider.class);
    private static final Validator VALIDATOR = new Validator();

    private final Json json;

    public JacksonMessageBodyProvider(Json json) {
        this.json = json;
    }

    @Override
    public boolean isReadable(Class<?> type,
                              Type genericType,
                              Annotation[] annotations,
                              MediaType mediaType) {
        return json.canDeserialize(type);
    }

    @Override
    public Object readFrom(Class<Object> type,
                           Type genericType,
                           Annotation[] annotations,
                           MediaType mediaType,
                           MultivaluedMap<String, String> httpHeaders,
                           InputStream entityStream) throws IOException, WebApplicationException {
        boolean validating = false;
        for (Annotation annotation : annotations) {
            validating = validating || (annotation.annotationType() == Valid.class);
        }

        final Object value = parseEntity(genericType, entityStream);
        if (validating) {
            final ImmutableList<String> errors = VALIDATOR.validate(value);
            if (!errors.isEmpty()) {
                throw new InvalidEntityException("The request entity had the following errors:",
                                                 errors);
            }
        }
        return value;
    }

    private Object parseEntity(Type genericType, InputStream entityStream) throws IOException {
        try {
            return json.readValue(entityStream, genericType);
        } catch (JsonProcessingException e) {
            throw new WebApplicationException(e, Response.Status.BAD_REQUEST);
        }
    }

    @Override
    public boolean isWriteable(Class<?> type,
                               Type genericType,
                               Annotation[] annotations,
                               MediaType mediaType) {
        return json.canSerialize(type);
    }

    @Override
    public long getSize(Object t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(Object t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream) throws IOException, WebApplicationException {
        try {
            json.writeValue(entityStream, t);
        } catch (EofException ignored) {
            // we don't care about these
        } catch (IOException e) {
            LOG.error(e, "Error writing response");
        }
    }
}
