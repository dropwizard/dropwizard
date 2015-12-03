package io.dropwizard.jersey.validation;

import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.jackson.JacksonMessageBodyProvider;

import javax.ws.rs.ext.Provider;

@Provider
public class DefaultJacksonMessageBodyProvider extends JacksonMessageBodyProvider {
    public DefaultJacksonMessageBodyProvider() {
        super(Jackson.newObjectMapper());
    }
}

