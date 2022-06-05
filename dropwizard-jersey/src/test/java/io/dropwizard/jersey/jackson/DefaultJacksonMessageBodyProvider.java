package io.dropwizard.jersey.jackson;

import io.dropwizard.jackson.DefaultObjectMapperFactory;

import javax.ws.rs.ext.Provider;

@Provider
public class DefaultJacksonMessageBodyProvider extends JacksonMessageBodyProvider {
    public DefaultJacksonMessageBodyProvider() {
        super(new DefaultObjectMapperFactory().newObjectMapper());
    }
}
