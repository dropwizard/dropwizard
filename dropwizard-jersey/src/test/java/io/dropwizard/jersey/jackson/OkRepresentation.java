package io.dropwizard.jersey.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;

public class OkRepresentation {
    private String message;

    @JsonProperty
    public String getMessage() {
        return message;
    }

    @JsonProperty
    public void setMessage(String message) {
        this.message = message;
    }
}
