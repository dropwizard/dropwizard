package com.example.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.UriInfo;

public class WasInjectedConstraintValidator implements ConstraintValidator<WasInjected, String> {

    @Context
    private UriInfo uriInfo;

    @Override
    public void initialize(WasInjected constraintAnnotation) {

    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return uriInfo != null;
    }
}
