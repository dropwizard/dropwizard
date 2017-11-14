package io.dropwizard.validation;

import io.dropwizard.util.Duration;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.concurrent.TimeUnit;

/**
 * Check that a {@link Duration} being validated is less than or equal to the
 * minimum value specified.
 */
public class MaxDurationValidator implements ConstraintValidator<MaxDuration, Duration> {

    private long maxQty = 0;
    private TimeUnit maxUnit = TimeUnit.MILLISECONDS;
    private boolean inclusive = true;

    @Override
    public void initialize(MaxDuration constraintAnnotation) {
        this.maxQty = constraintAnnotation.value();
        this.maxUnit = constraintAnnotation.unit();
        this.inclusive = constraintAnnotation.inclusive();
    }

    @Override
    public boolean isValid(Duration value, ConstraintValidatorContext context) {
        if (value == null) {
            return true;
        }

        long valueNanos = value.toNanoseconds();
        long annotationNanos = maxUnit.toNanos(maxQty);

        if (inclusive) {
            return valueNanos <= annotationNanos;
        } else {
            return valueNanos < annotationNanos;
        }
    }
}
