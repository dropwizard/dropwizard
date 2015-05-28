package io.dropwizard.jersey.validation;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.Valid;

public class WrappedValidRepresentation {
    @Valid
    private ValidRepresentation representation;

    @JsonProperty
    public ValidRepresentation getRepresentation() {
        return representation;
    }

    @JsonProperty
    public void setRepresentation(ValidRepresentation representation) {
        this.representation = representation;
    }
}
