package com.yammer.dropwizard.validation;

import com.google.common.collect.ImmutableList;

import javax.validation.ConstraintViolation;

public class ValidationResult<T> {

    private ImmutableList<ConstraintViolation<T>> violations;
    private ImmutableList<String> messages;

    ValidationResult (ImmutableList<ConstraintViolation<T>> violations, ImmutableList<String> messages) {
        this.violations = violations;
        this.messages = messages;
    }

    public ImmutableList<ConstraintViolation<T>> getViolations() { return violations; }

    public ImmutableList<String> getMessages() { return messages; }

    public boolean isEmpty () { return messages.isEmpty(); }

    @Override
    public String toString() { return messages.toString(); }
}
