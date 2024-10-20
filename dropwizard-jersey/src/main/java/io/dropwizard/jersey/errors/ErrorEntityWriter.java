package io.dropwizard.jersey.errors;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.MultivaluedMap;
import jakarta.ws.rs.ext.MessageBodyWriter;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.glassfish.jersey.message.MessageBodyWorkers;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

import static java.util.Objects.requireNonNull;

/**
 * This class allows producing non-JSON responses for particular entities. For example, register an instance with the
 * {@link ErrorMessage} entity and the TEXT_HTML MediaType to produce custom HTML error messages.
 *
 * @param <T> The entity type to handle
 * @param <U> The response type to produce
 */
public abstract class ErrorEntityWriter<T, U> implements MessageBodyWriter<T> {

    /**
     * @param contentType Content type the writer will produce
     * @param representation Response type the writer will produce
     */
    protected ErrorEntityWriter(MediaType contentType, Class<U> representation) {
        this.contentType = contentType;
        this.representation = representation;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return requireNonNull(headers).getAcceptableMediaTypes().contains(contentType);
    }

    @Override
    public long getSize(T entity, Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return 0;
    }

    @Override
    public void writeTo(T entity,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> responseHeaders,
                        OutputStream entityStream)
        throws IOException, WebApplicationException {

        final MessageBodyWriter<U> writer = requireNonNull(mbw).get().getMessageBodyWriter(representation,
            representation, annotations, contentType);

        // Fix the headers, because Dropwizard error mappers always set the content type to APPLICATION_JSON
        responseHeaders.putSingle(HttpHeaders.CONTENT_TYPE, contentType);

        writer.writeTo(getRepresentation(entity), representation, representation, annotations,
            contentType, responseHeaders, entityStream);
    }

    protected abstract U getRepresentation(T entity);

    private MediaType contentType;
    private Class<U> representation;

    @Context
    @Nullable
    private HttpHeaders headers;

    @Context
    private jakarta.inject.@Nullable Provider<MessageBodyWorkers> mbw;
}
