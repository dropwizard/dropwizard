package io.dropwizard.jersey.validation;

import io.dropwizard.jersey.params.AbstractParam;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorDescriptor;

import javax.validation.valueextraction.ExtractedValue;
import javax.validation.valueextraction.UnwrapByDefault;
import javax.validation.valueextraction.ValueExtractor;

/**
 * Let's the validator know that when validating a class that is an {@link AbstractParam} to
 * validate the underlying value.
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
