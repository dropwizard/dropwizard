package io.dropwizard.jersey.optional;

import org.glassfish.jersey.message.MessageBodyWorkers;

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;
import javax.ws.rs.ext.MessageBodyWriter;
import javax.ws.rs.ext.Provider;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Optional;

@Provider
@Produces(MediaType.WILDCARD)
public class OptionalMessageBodyWriter implements MessageBodyWriter<Optional<?>> {

    @Inject
    private javax.inject.Provider<MessageBodyWorkers> mbw;

    // Jersey ignores this
    @Override
    public long getSize(Optional<?> entity, Class<?> type, Type genericType,
                        Annotation[] annotations, MediaType mediaType) {
        return 0;
    }

    @Override
    public boolean isWriteable(Class<?> type, Type genericType,
                               Annotation[] annotations, MediaType mediaType) {
        return (Optional.class.isAssignableFrom(type));
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public void writeTo(Optional<?> entity,
                        Class<?> type,
                        Type genericType,
                        Annotation[] annotations,
                        MediaType mediaType,
                        MultivaluedMap<String, Object> httpHeaders,
                        OutputStream entityStream)
            throws IOException {
        if (!entity.isPresent()) {
            throw new NotFoundException();
        }

        final Type innerGenericType = (genericType instanceof ParameterizedType) ?
            ((ParameterizedType) genericType).getActualTypeArguments()[0] : entity.get().getClass();

        final MessageBodyWriter writer = mbw.get().getMessageBodyWriter(entity.get().getClass(),
            innerGenericType, annotations, mediaType);
        writer.writeTo(entity.get(), entity.get().getClass(),
            innerGenericType, annotations, mediaType, httpHeaders, entityStream);
    }

}
