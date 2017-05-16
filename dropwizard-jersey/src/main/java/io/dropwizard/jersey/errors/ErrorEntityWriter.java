package io.dropwizard.jersey.errors;

import org.glassfish.jersey.message.MessageBodyWorkers;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

/**
 * This class allows producing non-JSON responses for particular entities. For example, register a instance with the
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
    public ErrorEntityWriter(MediaType contentType, Class<U> representation) {
        this.contentType = contentType;
        this.representation = representation;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType, Annotation[] annotations, MediaType mediaType) {
        return headers.getAcceptableMediaTypes().contains(contentType);
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

        final MessageBodyWriter<U> writer = mbw.get().getMessageBodyWriter(representation,
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
    private HttpHeaders headers;

    @Context
    private javax.inject.Provider<MessageBodyWorkers> mbw;
}
