package io.dropwizard.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Check that the string representation of an object is in a given set of valid values.
 */
public class OneOfValidator implements ConstraintValidator<OneOf, Object> {
    private String[] values = new String[]{};
    private boolean caseInsensitive;
    private boolean ignoreWhitespace;

    @Override
    public void initialize(OneOf constraintAnnotation) {
        this.values = constraintAnnotation.value();
        this.caseInsensitive = constraintAnnotation.ignoreCase();
        this.ignoreWhitespace = constraintAnnotation.ignoreWhitespace();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }
        final String v = ignoreWhitespace ? value.toString().trim() : value.toString();
        if (caseInsensitive) {
            for (String s : values) {
                if (s.equalsIgnoreCase(v)) {
                    return true;
                }
            }
        } else {
            for (String s : values) {
                if (s.equals(v)) {
                    return true;
                }
            }
        }
        return false;
    }
}
