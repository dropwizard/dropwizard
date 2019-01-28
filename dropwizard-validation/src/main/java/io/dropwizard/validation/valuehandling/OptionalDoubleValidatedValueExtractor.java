package io.dropwizard.validation.valuehandling;

import javax.validation.valueextraction.ExtractedValue;
import javax.validation.valueextraction.ValueExtractor;
import java.util.OptionalDouble;

/**
 * A {@link ValueExtractor} for {@link OptionalDouble}.
 *
 * Extracts the value contained by the {@link OptionalDouble} for validation, or produces {@code null}.
 */
public class OptionalDoubleValidatedValueExtractor implements ValueExtractor<@ExtractedValue(type = Double.class) OptionalDouble> {

    @Override
    public void extractValues(OptionalDouble originalValue, ValueReceiver receiver) {
        receiver.value(null, originalValue.isPresent() ? originalValue.getAsDouble() : null);
    }
}
