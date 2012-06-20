package com.yammer.dropwizard.validation;

import com.yammer.dropwizard.util.Size;
import com.yammer.dropwizard.util.SizeUnit;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Check that a {@link Size} being validated is greater than or equal to the
 * minimum value specified.
 */
public class MinSizeValidator implements ConstraintValidator<MinSize, Size> {

    private long minQty;
    private SizeUnit minUnit;

    @Override
    public void initialize(MinSize minValue) {
        this.minQty = minValue.value();
        this.minUnit = minValue.unit();
    }

    @Override
    public boolean isValid(Size value, ConstraintValidatorContext constraintValidatorContext) {
        return value == null || value.toBytes() >= minUnit.toBytes(minQty);
    }
}
