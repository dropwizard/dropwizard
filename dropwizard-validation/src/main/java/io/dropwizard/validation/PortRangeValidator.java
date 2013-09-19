package io.dropwizard.validation;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Allow 0 to indicate dynamic port range allocation. If not zero, it must be within the {min,max}
 * range, inclusive.
 */
public class PortRangeValidator implements ConstraintValidator<PortRange, Integer> {
    private int min;
    private int max;

    @Override
    public void initialize(PortRange constraintAnnotation) {
        this.min = constraintAnnotation.min();
        this.max = constraintAnnotation.max();
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        return value == 0 || (value >= min && value <= max);
    }
}
