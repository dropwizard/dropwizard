package io.dropwizard.jersey.validation;

import io.dropwizard.jersey.params.NonEmptyStringParam;
import org.hibernate.validator.spi.valuehandling.ValidatedValueUnwrapper;

import java.lang.reflect.Type;

/**
 * Let's the validator know that when validating a {@link NonEmptyStringParam} to validate the
 * underlying value. This class is needed, temporarily, while Hibernate is not able to unwrap nested
 * classes <a href="https://hibernate.atlassian.net/browse/HV-904"/>.
 */
public class NonEmptyStringParamUnwrapper extends ValidatedValueUnwrapper<NonEmptyStringParam> {
    @Override
    public Object handleValidatedValue(final NonEmptyStringParam nonEmptyStringParam) {
        return nonEmptyStringParam.get().orElse(null);
    }

    @Override
    public Type getValidatedValueType(final Type type) {
        return String.class;
    }
}
