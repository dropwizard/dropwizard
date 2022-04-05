package io.dropwizard.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * A validator for {@link ValidationMethod}-annotated methods.
 */
public class MethodValidator implements ConstraintValidator<ValidationMethod, Boolean> {
    @Override
    public boolean isValid(Boolean value, ConstraintValidatorContext context) {
        return (value == null) || value;
    }
}
