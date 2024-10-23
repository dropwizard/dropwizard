package io.dropwizard.jersey.validation;

import io.dropwizard.jersey.params.NonEmptyStringParam;
import jakarta.validation.valueextraction.ExtractedValue;
import jakarta.validation.valueextraction.UnwrapByDefault;
import jakarta.validation.valueextraction.ValueExtractor;
import org.hibernate.validator.internal.engine.valueextraction.ValueExtractorDescriptor;

/**
 * Lets the validator know that when validating a {@link NonEmptyStringParam} to validate the
 * underlying value. This class is needed, temporarily, while Hibernate is not able to unwrap nested
 * classes <a href="https://hibernate.atlassian.net/browse/HV-904"/>.
 *
 * @since 2.0
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
