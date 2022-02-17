package io.dropwizard.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * A validator for {@link ValidationMethod}-annotated methods.
 */
public class MethodValidator implements ConstraintValidator<ValidationMethod, Boolean> {
    @Override
    public void initialize(ValidationMethod constraintAnnotation) {

    }

    @Override
    public boolean isValid(Boolean value, ConstraintValidatorContext context) {
        return (value == null) || value;
    }
}
