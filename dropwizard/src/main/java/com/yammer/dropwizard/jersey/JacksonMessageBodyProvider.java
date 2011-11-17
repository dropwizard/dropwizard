package com.yammer.dropwizard.jersey;

import com.google.common.collect.ImmutableList;
import com.yammer.dropwizard.json.Json;
import com.yammer.dropwizard.util.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

// TODO: 10/12/11 <coda> -- write tests for JacksonMessageBodyProvider
// TODO: 10/12/11 <coda> -- write docs for JacksonMessageBodyProvider

@Provider
@Produces("application/json")
@Consumes("application/json")
public class JacksonMessageBodyProvider implements MessageBodyReader<Object>,
                                                   MessageBodyWriter<Object> {
    private static final Logger LOGGER = LoggerFactory.getLogger(JacksonMessageBodyProvider.class);
    private static final Validator VALIDATOR = new Validator();
    private static final Response.StatusType UNPROCESSABLE_ENTITY = new Response.StatusType() {
        @Override
        public int getStatusCode() {
            return 422;
        }

        @Override
        public Response.Status.Family getFamily() {
            return Response.Status.Family.CLIENT_ERROR;
        }

        @Override
        public String getReasonPhrase() {
            return "Unprocessable Entity";
        }
    };

    @Override
    public boolean isReadable(Class<?> type,
                              Type genericType,
                              Annotation[] annotations,
                              MediaType mediaType) {
        return Json.canDeserialize(type);
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

        final Object value = Json.read(entityStream, genericType);
        if (validating) {
            final ImmutableList<String> errors = VALIDATOR.validate(value);
            if (!errors.isEmpty()) {
                final StringBuilder msg = new StringBuilder("The request entity had the following errors:\n");
                for (String error : errors) {
                    msg.append("  * ").append(error).append('\n');
                }
                throw new WebApplicationException(Response.status(UNPROCESSABLE_ENTITY)
                                                          .entity(msg.toString())
                                                          .type(MediaType.TEXT_PLAIN_TYPE)
                                                          .build());
            }
        }
        return value;
    }

    @Override
    public boolean isWriteable(Class<?> type,
                               Type genericType,
                               Annotation[] annotations,
                               MediaType mediaType) {
        return Json.canSerialize(type);
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
            Json.write(entityStream, t);
        } catch (IOException e) {
            LOGGER.error("Error writing response", e);
        }
    }
}
