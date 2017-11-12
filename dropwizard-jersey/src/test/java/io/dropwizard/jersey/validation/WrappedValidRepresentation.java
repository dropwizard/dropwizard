package io.dropwizard.jersey.validation;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.annotation.Nullable;
import javax.validation.Valid;

public class WrappedValidRepresentation {
    @Valid
    @Nullable
    private ValidRepresentation representation;

    @JsonProperty
    @Nullable
    public ValidRepresentation getRepresentation() {
        return representation;
    }

    @JsonProperty
    public void setRepresentation(ValidRepresentation representation) {
        this.representation = representation;
    }
}
