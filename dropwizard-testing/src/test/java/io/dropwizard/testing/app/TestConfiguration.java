package io.dropwizard.testing.app;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.core.Configuration;
import javax.validation.constraints.NotEmpty;

public class TestConfiguration extends Configuration {

    @JsonProperty
    @NotEmpty
    private String message = "";

    @JsonProperty
    @NotEmpty
    private String extra = "";

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
