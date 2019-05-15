package io.dropwizard.jersey.validation;

import io.dropwizard.jersey.params.NonEmptyStringParam;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorDescriptor;

import javax.validation.valueextraction.ExtractedValue;
import javax.validation.valueextraction.UnwrapByDefault;
import javax.validation.valueextraction.ValueExtractor;

/**
 * Let's the validator know that when validating a {@link NonEmptyStringParam} to validate the
 * underlying value. This class is needed, temporarily, while Hibernate is not able to unwrap nested
 * classes <a href="https://hibernate.atlassian.net/browse/HV-904"/>.
 */
@UnwrapByDefault
public class NonEmptyStringParamValueExtractor implements ValueExtractor<@ExtractedValue(type = String.class) NonEmptyStringParam> {
    static final ValueExtractorDescriptor DESCRIPTOR = new ValueExtractorDescriptor(new NonEmptyStringParamValueExtractor());

    private NonEmptyStringParamValueExtractor() {
    }

    @Override
    public void extractValues(NonEmptyStringParam originalValue, ValueExtractor.ValueReceiver receiver) {
        receiver.value(null, originalValue == null ? null : originalValue.get().orElse(null));
    }
}
