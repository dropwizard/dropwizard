package io.dropwizard.jersey.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.ws.rs.core.Feature;
import jakarta.ws.rs.core.FeatureContext;
import jakarta.ws.rs.ext.MessageBodyReader;
import jakarta.ws.rs.ext.MessageBodyWriter;

/**
 * A binder that registers the Jackson JSON provider while allowing users to override.
 *
 * @since 2.0
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
