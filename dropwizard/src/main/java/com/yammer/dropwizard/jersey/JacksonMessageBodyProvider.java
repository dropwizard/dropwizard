package com.yammer.dropwizard.jersey;

import com.yammer.dropwizard.json.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;
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
import java.util.Set;

// TODO: 10/12/11 <coda> -- write tests for JacksonMessageBodyProvider
// TODO: 10/12/11 <coda> -- write docs for JacksonMessageBodyProvider

@Provider
@Produces("application/json")
@Consumes("application/json")
public class JacksonMessageBodyProvider<T> implements MessageBodyReader<T>,
                                                      MessageBodyWriter<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(JacksonMessageBodyProvider.class);
    private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();
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
    public T readFrom(Class<T> type,
                      Type genericType,
                      Annotation[] annotations,
                      MediaType mediaType,
                      MultivaluedMap<String, String> httpHeaders,
                      InputStream entityStream) throws IOException, WebApplicationException {
        final T value = Json.read(entityStream, genericType);
        final Validator validator = VALIDATOR_FACTORY.getValidator();
        final Set<ConstraintViolation<T>> violations = validator.validate(value);
        if (!violations.isEmpty()) {
            final StringBuilder msg = new StringBuilder("The request entity had the following errors:\n");
            for (ConstraintViolation<?> v : violations) {
                msg.append("  * ").append(v.getPropertyPath()).append(" ").append(v.getMessage());
            }
            throw new WebApplicationException(Response.status(UNPROCESSABLE_ENTITY)
                                                      .entity(msg.toString())
                                                      .type(MediaType.TEXT_PLAIN_TYPE)
                                                      .build());
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
    public long getSize(T t,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType) {
        return -1;
    }

    @Override
    public void writeTo(T t,
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
