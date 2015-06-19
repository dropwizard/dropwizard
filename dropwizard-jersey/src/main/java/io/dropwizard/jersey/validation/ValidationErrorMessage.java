package io.dropwizard.jersey.validation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

public class ValidationErrorMessage {
    private final ImmutableList<String> errors;

    @JsonCreator
    public ValidationErrorMessage(@JsonProperty("errors") ImmutableList<String> errors) {
        this.errors = errors;
    }

    @JsonProperty
    public ImmutableList<String> getErrors() {
        return errors;
    }
}
