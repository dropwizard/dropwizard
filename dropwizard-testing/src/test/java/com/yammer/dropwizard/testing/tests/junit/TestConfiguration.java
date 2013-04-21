package com.yammer.dropwizard.testing.tests.junit;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.yammer.dropwizard.config.Configuration;
import org.hibernate.validator.constraints.NotEmpty;

public class TestConfiguration extends Configuration {

    @JsonProperty
    @NotEmpty
    private String message;

    public String getMessage() {
        return message;
    }
}
