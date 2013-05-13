package com.codahale.dropwizard.jersey.validation;

import com.codahale.dropwizard.validation.ConstraintViolations;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableList;

import javax.validation.ConstraintViolation;
import java.util.Set;

public class ValidationErrorMessage {
    private final ImmutableList<String> errors;

    public ValidationErrorMessage(Set<ConstraintViolation<?>> errors) {
        this.errors = ConstraintViolations.formatUntyped(errors);
    }

    @JsonProperty
    public ImmutableList<String> getErrors() {
        return errors;
    }
}
