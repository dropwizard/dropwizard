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

    private long minQty = 0;
    private TimeUnit minUnit = TimeUnit.MILLISECONDS;
    private boolean inclusive = true;

    @Override
    public void initialize(MinDuration constraintAnnotation) {
        this.minQty = constraintAnnotation.value();
        this.minUnit = constraintAnnotation.unit();
        this.inclusive = constraintAnnotation.inclusive();
    }

    @Override
    public boolean isValid(Duration value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        long valueNanos = value.toNanoseconds();
        long annotationNanos = minUnit.toNanos(minQty);

        if (inclusive) {
            return valueNanos >= annotationNanos;
        } else {
            return valueNanos > annotationNanos;
        }
    }
}
