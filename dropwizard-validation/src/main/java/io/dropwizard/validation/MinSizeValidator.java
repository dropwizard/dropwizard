package io.dropwizard.validation;

import io.dropwizard.util.Size;
import io.dropwizard.util.SizeUnit;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Check that a {@link Size} being validated is greater than or equal to the
 * minimum value specified.
 *
 * @deprecated Use {@link MinDataSizeValidator} for correct SI and IEC prefixes.
 */
@Deprecated
public class MinSizeValidator implements ConstraintValidator<MinSize, Size> {

    private long minQty = 0;
    private SizeUnit minUnit = SizeUnit.BYTES;

    @Override
    public void initialize(MinSize constraintAnnotation) {
        this.minQty = constraintAnnotation.value();
        this.minUnit = constraintAnnotation.unit();
    }

    @Override
    public boolean isValid(Size value, ConstraintValidatorContext context) {
        return (value == null) || (value.toBytes() >= minUnit.toBytes(minQty));
    }
}
