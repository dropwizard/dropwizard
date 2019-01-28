package io.dropwizard.validation.valuehandling;


import javax.validation.valueextraction.ExtractedValue;
import javax.validation.valueextraction.ValueExtractor;
import java.util.OptionalInt;

/**
 * A {@link ValueExtractor} for {@link OptionalInt}.
 *
 * Extracts the value contained by the {@link OptionalInt} for validation, or produces {@code null}.
 */
public class OptionalIntValidatedValueExtractor implements ValueExtractor<@ExtractedValue(type = Integer.class) OptionalInt> {

    @Override
    public void extractValues(OptionalInt originalValue, ValueReceiver receiver) {
        receiver.value(null, originalValue.isPresent() ? originalValue.getAsInt() : null);
    }
}
