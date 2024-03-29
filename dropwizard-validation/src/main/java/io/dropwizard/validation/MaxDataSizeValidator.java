package io.dropwizard.validation;

import io.dropwizard.util.DataSize;
import io.dropwizard.util.DataSizeUnit;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Check that a {@link DataSize} being validated is less than or equal to the
 * minimum value specified.
 *
 * @since 2.0
 */
public class MaxDataSizeValidator implements ConstraintValidator<MaxDataSize, DataSize> {

    private long maxQty = 0;
    private DataSizeUnit maxUnit = DataSizeUnit.BYTES;

    @Override
    public void initialize(MaxDataSize constraintAnnotation) {
        this.maxQty = constraintAnnotation.value();
        this.maxUnit = constraintAnnotation.unit();
    }

    @Override
    public boolean isValid(DataSize value, ConstraintValidatorContext context) {
        return (value == null) || (value.toBytes() <= maxUnit.toBytes(maxQty));
    }
}
