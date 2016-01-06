package io.dropwizard.jersey.validation;


import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import io.dropwizard.jersey.params.AbstractParam;
import org.hibernate.validator.spi.valuehandling.ValidatedValueUnwrapper;

import java.lang.reflect.Type;

/**
 * Let's the validator know that when validating a class that is an {@link AbstractParam} to
 * validate the underlying value.
 */
public class ParamValidatorUnwrapper extends ValidatedValueUnwrapper<AbstractParam<?>> {
    private final TypeResolver resolver = new TypeResolver();

    @Override
    public Object handleValidatedValue(final AbstractParam<?> abstractParam) {
        return abstractParam == null ? null : abstractParam.get();
    }

    @Override
    public Type getValidatedValueType(final Type type) {
        return resolver.resolve(type)
                .typeParametersFor(AbstractParam.class).get(0)
                .getErasedType();
    }
}
