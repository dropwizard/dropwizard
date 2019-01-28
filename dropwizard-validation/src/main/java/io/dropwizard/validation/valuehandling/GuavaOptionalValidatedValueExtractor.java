package io.dropwizard.validation.valuehandling;

import com.google.common.base.Optional;

import javax.validation.valueextraction.ExtractedValue;
import javax.validation.valueextraction.ValueExtractor;

/**
 * A {@link ValueExtractor} for Guava's {@link Optional}.
 *
 * Extracts the value contained by the {@link Optional} for validation, or produces {@code null}.
 */
public class GuavaOptionalValidatedValueExtractor implements ValueExtractor<Optional<@ExtractedValue ?>> {

    @Override
    public void extractValues(Optional<?> optional, ValueReceiver valueReceiver) {
        valueReceiver.value(null, optional.orNull());
    }
}
