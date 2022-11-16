package io.dropwizard.validation.valuehandling;

import com.google.common.base.Optional;
import jakarta.validation.valueextraction.ExtractedValue;
import jakarta.validation.valueextraction.ValueExtractor;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorDescriptor;

/**
 * A {@link ValueExtractor} for Guava's {@link Optional}.
 *
 * Extracts the value contained by the {@link Optional} for validation, or produces {@code null}.
 *
 * @since 2.0
 */
public class GuavaOptionalValueExtractor implements ValueExtractor<Optional<@ExtractedValue ?>> {
    /**
     * A singleton {@link ValueExtractorDescriptor} for the {@link GuavaOptionalValueExtractor}.
     */
    public static final ValueExtractorDescriptor DESCRIPTOR = new ValueExtractorDescriptor(new GuavaOptionalValueExtractor());

    private GuavaOptionalValueExtractor() {
    }

    @Override
    public void extractValues(Optional<?> originalValue, ValueExtractor.ValueReceiver receiver) {
        receiver.value(null, originalValue.orNull());
    }
}
