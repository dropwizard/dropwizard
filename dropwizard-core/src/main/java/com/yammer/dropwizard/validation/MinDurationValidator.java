package com.yammer.dropwizard.validation;

import com.yammer.dropwizard.util.Duration;

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
    public void initialize(MinDuration minValue) {
        this.minQty = minValue.value();
        this.minUnit = minValue.unit();
    }

    @Override
    public boolean isValid(Duration value, ConstraintValidatorContext constraintValidatorContext) {
        return value == null || value.toNanoseconds() >= minUnit.toNanos(minQty);
    }
}
