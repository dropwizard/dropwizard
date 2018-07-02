package io.dropwizard.jersey.validation;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

public class ValidationErrorMessage {
    private final List<String> errors;

    @JsonCreator
    public ValidationErrorMessage(@JsonProperty("errors") List<String> errors) {
        this.errors = errors;
    }

    @JsonProperty
    public List<String> getErrors() {
        return errors;
    }
}
