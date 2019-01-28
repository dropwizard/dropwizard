package io.dropwizard.jersey.validation;


import io.dropwizard.jersey.params.AbstractParam;

import javax.validation.valueextraction.ExtractedValue;
import javax.validation.valueextraction.ValueExtractor;

/**
 * Let's the validator know that when validating a class that is an {@link AbstractParam} to
 * validate the underlying value.
 */
public class ParamValidatorValueExtractor implements ValueExtractor<AbstractParam<@ExtractedValue ?>> {

    @Override
    public void extractValues(AbstractParam<?> originalValue, ValueReceiver receiver) {
        receiver.value(null, originalValue == null ? null : originalValue.get());
    }
}
