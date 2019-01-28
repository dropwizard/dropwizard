package io.dropwizard.jersey.validation;

import io.dropwizard.jersey.params.NonEmptyStringParam;

import javax.validation.valueextraction.ExtractedValue;
import javax.validation.valueextraction.ValueExtractor;

/**
 * Let's the validator know that when validating a {@link NonEmptyStringParam} to validate the
 * underlying value. This class is needed, temporarily, while Hibernate is not able to unwrap nested
 * classes <a href="https://hibernate.atlassian.net/browse/HV-904"/>.
 */
public class NonEmptyStringParamExtractor implements ValueExtractor<@ExtractedValue(type = String.class) NonEmptyStringParam> {

    @Override
    public void extractValues(NonEmptyStringParam originalValue, ValueReceiver receiver) {
        receiver.value(null, originalValue.get().orElse(null));
    }
}
