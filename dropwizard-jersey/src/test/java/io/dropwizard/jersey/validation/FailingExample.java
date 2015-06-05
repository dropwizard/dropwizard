package io.dropwizard.jersey.validation;

import io.dropwizard.validation.ValidationMethod;

public class FailingExample {
    @ValidationMethod(message = "must have a false thing")
    public boolean isFail() {
        return false;
    }
}
