package io.dropwizard.jersey.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.MessageBodyReader;
import javax.ws.rs.ext.MessageBodyWriter;

/**
 * An HK2 binder that registers the Jackson JSON provider while allowing users to override.
 */
public class JacksonFeature implements Feature {
    private final ObjectMapper mapper;

    public JacksonFeature(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public boolean configure(FeatureContext context) {
        context.register(new JacksonMessageBodyProvider(mapper), MessageBodyReader.class, MessageBodyWriter.class);
        return true;
    }
}
