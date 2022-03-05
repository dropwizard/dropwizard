package io.dropwizard.validation;

import io.dropwizard.util.Size;
import io.dropwizard.util.SizeUnit;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Check that a {@link Size} being validated is less than or equal to the
 * minimum value specified.
 *
 * @deprecated Use {@link MaxDataSizeValidator} for correct SI and IEC prefixes.
 */
@Deprecated
public class MaxSizeValidator implements ConstraintValidator<MaxSize, Size> {

    private long maxQty = 0;
    private SizeUnit maxUnit = SizeUnit.BYTES;

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
