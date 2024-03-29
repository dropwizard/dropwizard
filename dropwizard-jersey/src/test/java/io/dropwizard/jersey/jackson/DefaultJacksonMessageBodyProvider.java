package io.dropwizard.jersey.jackson;

import io.dropwizard.jackson.Jackson;
import jakarta.ws.rs.ext.Provider;

@Provider
public class DefaultJacksonMessageBodyProvider extends JacksonMessageBodyProvider {
    public DefaultJacksonMessageBodyProvider() {
        super(Jackson.newObjectMapper());
    }
}
