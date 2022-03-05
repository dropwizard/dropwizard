package com.example.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.UriInfo;

public class WasInjectedConstraintValidator implements ConstraintValidator<WasInjected, String> {

    @Context
    private UriInfo uriInfo;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return uriInfo != null;
    }
}
