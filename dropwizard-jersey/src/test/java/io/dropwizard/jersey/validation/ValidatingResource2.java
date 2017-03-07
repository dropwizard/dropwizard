package io.dropwizard.jersey.validation;

import io.dropwizard.other.RestInterface;

public class ValidatingResource2 implements RestInterface {
    @Override
    public ValidRepresentation repr(ValidRepresentation representation, String xer) {
        return representation;
    }
}
