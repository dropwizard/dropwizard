package io.dropwizard.jersey.validation;

import io.dropwizard.jersey.params.AbstractParam;
import jakarta.validation.valueextraction.ExtractedValue;
import jakarta.validation.valueextraction.UnwrapByDefault;
import jakarta.validation.valueextraction.ValueExtractor;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorDescriptor;

/**
 * Lets the validator know that when validating a class that is an {@link AbstractParam} to
 * validate the underlying value.
 *
 * @since 2.0
 */
@UnwrapByDefault
public class ParamValueExtractor implements ValueExtractor<AbstractParam<@ExtractedValue ?>> {
    static final ValueExtractorDescriptor DESCRIPTOR = new ValueExtractorDescriptor(new ParamValueExtractor());

    private ParamValueExtractor() {
    }

    @Override
    public void extractValues(AbstractParam<?> originalValue, ValueExtractor.ValueReceiver receiver) {
        receiver.value(null, originalValue == null ? null : originalValue.get());
    }
}
