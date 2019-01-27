package io.dropwizard.validation.valuehandling;

import com.google.common.base.Optional;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorDescriptor;

import javax.validation.valueextraction.ExtractedValue;
import javax.validation.valueextraction.ValueExtractor;

/**
 * A {@link ValueExtractor} for Guava's {@link Optional}.
 *
 * Extracts the value contained by the {@link Optional} for validation, or produces {@code null}.
 */
public class GuavaOptionalValueExtractor implements ValueExtractor<Optional<@ExtractedValue ?>> {
    public static final ValueExtractorDescriptor DESCRIPTOR = new ValueExtractorDescriptor(new GuavaOptionalValueExtractor());

    private GuavaOptionalValueExtractor() {
    }

    @Override
    public void extractValues(Optional<?> originalValue, ValueExtractor.ValueReceiver receiver) {
        receiver.value(null, originalValue.orNull());
    }
}
