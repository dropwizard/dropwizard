package io.dropwizard.jersey.jackson;

import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;

import javax.ws.rs.ext.Provider;

@Provider
public class DefaultJacksonMessageBodyProvider extends JacksonMessageBodyProvider {
    public DefaultJacksonMessageBodyProvider() {
        super(Jackson.newObjectMapper());
    }
}
