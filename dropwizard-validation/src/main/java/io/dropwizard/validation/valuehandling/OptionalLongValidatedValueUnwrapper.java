package io.dropwizard.validation.valuehandling;

import org.hibernate.validator.spi.valuehandling.ValidatedValueUnwrapper;

import java.lang.reflect.Type;
import java.util.OptionalLong;

/**
 * A {@link ValidatedValueUnwrapper} for {@link OptionalLong}.
 *
 * Extracts the value contained by the {@link OptionalLong} for validation, or produces {@code null}.
 */
public class OptionalLongValidatedValueUnwrapper extends ValidatedValueUnwrapper<OptionalLong> {
    @Override
    public Object handleValidatedValue(final OptionalLong optional) {
        return optional.isPresent() ? optional.getAsLong() : null;
    }

    @Override
    public Type getValidatedValueType(final Type type) {
        return Long.class;
    }
}
