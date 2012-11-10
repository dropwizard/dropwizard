package com.yammer.dropwizard.validation;

import com.yammer.dropwizard.util.Size;
import com.yammer.dropwizard.util.SizeUnit;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

/**
 * Check that a {@link Size} being validated is less than or equal to the
 * minimum value specified.
 */
public class MaxSizeValidator implements ConstraintValidator<MaxSize, Size> {

    private long maxQty;
    private SizeUnit maxUnit;

    @Override
    public void initialize(MaxSize constraintAnnotation) {
        this.maxQty = constraintAnnotation.value();
        this.maxUnit = constraintAnnotation.unit();
    }

    @Override
    public boolean isValid(Size value, ConstraintValidatorContext context) {
        return (value == null) || (value.toBytes() <= maxUnit.toBytes(maxQty));
    }
}
