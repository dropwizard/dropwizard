package io.dropwizard.testing.junit;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.Configuration;
import org.hibernate.validator.constraints.NotEmpty;

public class TestConfiguration extends Configuration {

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
