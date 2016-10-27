package io.dropwizard.jersey.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.glassfish.hk2.utilities.binding.AbstractBinder;

import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

/**
 * An HK2 binder that registers the Jackson JSON provider while allowing users to override.
 */
public class JacksonBinder extends AbstractBinder {
    private final ObjectMapper mapper;

    public JacksonBinder(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    protected void configure() {
        final JacksonMessageBodyProvider jsonProvider = new JacksonMessageBodyProvider(mapper);
        bind(jsonProvider).to(MessageBodyWriter.class);
        bind(jsonProvider).to(MessageBodyReader.class);
    }
}
