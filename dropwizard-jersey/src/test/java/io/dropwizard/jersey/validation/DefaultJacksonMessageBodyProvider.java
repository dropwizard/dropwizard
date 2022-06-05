package io.dropwizard.jersey.validation;

import io.dropwizard.jackson.DefaultObjectMapperFactory;
import io.dropwizard.jersey.jackson.JacksonMessageBodyProvider;

import javax.ws.rs.ext.Provider;

@Provider
public class DefaultJacksonMessageBodyProvider extends JacksonMessageBodyProvider {
    public DefaultJacksonMessageBodyProvider() {
        super(new DefaultObjectMapperFactory().newObjectMapper());
    }
}

