package com.yammer.dropwizard.validation;

import com.google.common.collect.ImmutableList;

public class InvalidEntityException extends RuntimeException {
    private static final long serialVersionUID = -8762073181655035705L;

    private final ValidationResult result;
    private final ImmutableList<String> errors;

    public InvalidEntityException(String message, ValidationResult result) {
        super(message);
        this.result = result;
        this.errors = ImmutableList.copyOf(result.getMessages());
    }

    public ValidationResult getResult() { return result; }

    public ImmutableList<String> getErrors() { return errors; }

}
