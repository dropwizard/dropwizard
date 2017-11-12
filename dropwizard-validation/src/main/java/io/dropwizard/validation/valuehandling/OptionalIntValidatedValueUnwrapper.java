package io.dropwizard.validation.valuehandling;

import org.hibernate.validator.spi.valuehandling.ValidatedValueUnwrapper;

import javax.annotation.Nullable;
import java.lang.reflect.Type;
import java.util.OptionalInt;

/**
 * A {@link ValidatedValueUnwrapper} for {@link OptionalInt}.
 *
 * Extracts the value contained by the {@link OptionalInt} for validation, or produces {@code null}.
 */
public class OptionalIntValidatedValueUnwrapper extends ValidatedValueUnwrapper<OptionalInt> {
    @Override
    @Nullable
    public Object handleValidatedValue(final OptionalInt optional) {
        return optional.isPresent() ? optional.getAsInt() : null;
    }

    @Override
    public Type getValidatedValueType(final Type type) {
        return Integer.class;
    }
}
