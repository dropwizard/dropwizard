package io.dropwizard.validation.valuehandling;

import javax.validation.valueextraction.ExtractedValue;
import javax.validation.valueextraction.ValueExtractor;
import java.util.OptionalLong;

/**
 * A {@link ValueExtractor} for {@link OptionalLong}.
 *
 * Extracts the value contained by the {@link OptionalLong} for validation, or produces {@code null}.
 */
public class OptionalLongValidatedValueExtractor implements ValueExtractor<@ExtractedValue (type = Long.class) OptionalLong> {

    @Override
    public void extractValues(OptionalLong originalValue, ValueReceiver receiver) {
        receiver.value(null, originalValue.isPresent() ? originalValue.getAsLong() : null);
    }
}
