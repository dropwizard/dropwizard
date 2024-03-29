package io.dropwizard.validation;

import io.dropwizard.util.DataSize;
import io.dropwizard.util.DataSizeUnit;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Check that a {@link DataSize} being validated is greater than or equal to the
 * minimum value specified.
 *
 * @since 2.0
 */
public class MinDataSizeValidator implements ConstraintValidator<MinDataSize, DataSize> {

    private long minQty = 0;
    private DataSizeUnit minUnit = DataSizeUnit.BYTES;

    @Override
    public void initialize(MinDataSize constraintAnnotation) {
        this.minQty = constraintAnnotation.value();
        this.minUnit = constraintAnnotation.unit();
    }

    @Override
    public boolean isValid(DataSize value, ConstraintValidatorContext context) {
        return (value == null) || (value.toBytes() >= minUnit.toBytes(minQty));
    }
}
