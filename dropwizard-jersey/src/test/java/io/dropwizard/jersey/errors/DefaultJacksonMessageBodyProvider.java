package io.dropwizard.jersey.errors;

import io.dropwizard.jackson.DefaultObjectMapperFactory;
import io.dropwizard.jersey.jackson.JacksonMessageBodyProvider;

import javax.ws.rs.ext.Provider;

@Provider
public class DefaultJacksonMessageBodyProvider extends JacksonMessageBodyProvider {
    public DefaultJacksonMessageBodyProvider() {
        super(new DefaultObjectMapperFactory().newObjectMapper());
    }
}
