package com.codahale.dropwizard.testing.junit;

import com.codahale.dropwizard.ServerConfiguration;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.validator.constraints.NotEmpty;

public class TestConfiguration extends ServerConfiguration {

    @JsonProperty
    @NotEmpty
    private String message;

    public String getMessage() {
        return message;
    }
}
