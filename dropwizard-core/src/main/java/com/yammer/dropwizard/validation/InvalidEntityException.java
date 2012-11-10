package com.yammer.dropwizard.validation;

import com.google.common.collect.ImmutableList;

public class InvalidEntityException extends RuntimeException {
    private static final long serialVersionUID = -8762073181655035705L;

    private final ImmutableList<String> errors;

    public InvalidEntityException(String message, Iterable<String> errors) {
        super(message);
        this.errors = ImmutableList.copyOf(errors);
    }

    public ImmutableList<String> getErrors() {
        return errors;
    }
}
