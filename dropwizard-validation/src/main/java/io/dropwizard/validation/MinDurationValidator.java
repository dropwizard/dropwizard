package io.dropwizard.validation;

import io.dropwizard.util.Duration;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.concurrent.TimeUnit;

/**
 * Check that a {@link Duration} being validated is greater than or equal to the
 * minimum value specified.
 */
public class MinDurationValidator implements ConstraintValidator<MinDuration, Duration> {

    private long minQty;
    private TimeUnit minUnit;

    @Override
    public void initialize(MinDuration constraintAnnotation) {
        this.minQty = constraintAnnotation.value();
        this.minUnit = constraintAnnotation.unit();
    }

    @Override
    public boolean isValid(Duration value, ConstraintValidatorContext context) {
        return (value == null) || (value.toNanoseconds() >= minUnit.toNanos(minQty));
    }
}
