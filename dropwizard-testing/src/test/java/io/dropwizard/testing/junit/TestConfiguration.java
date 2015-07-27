package io.dropwizard.testing.junit;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import io.dropwizard.HttpConfiguration;
import io.dropwizard.server.DefaultServerFactory;
import io.dropwizard.server.ServerFactory;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

public class TestConfiguration extends HttpConfiguration {

    @JsonProperty
    @NotEmpty
    private String message;

    @JsonProperty
    @NotEmpty
    private String extra;

    public TestConfiguration() { }

    public TestConfiguration(String message, String extra) {
        this.message = message;
        this.extra = extra;
    }

    public String getMessage() {
        return message;
    }

    public String getExtra() {
        return extra;
    }
}
