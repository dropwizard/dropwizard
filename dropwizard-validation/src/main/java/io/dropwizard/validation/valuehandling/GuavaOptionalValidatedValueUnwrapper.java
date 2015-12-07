package io.dropwizard.validation.valuehandling;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import com.google.common.base.Optional;
import org.hibernate.validator.spi.valuehandling.ValidatedValueUnwrapper;

import java.lang.reflect.Type;

/**
 * A {@link ValidatedValueUnwrapper} for Guava's {@link Optional}.
 *
 * Extracts the value contained by the {@link Optional} for validation, or produces {@code null}.
 */
public class GuavaOptionalValidatedValueUnwrapper extends ValidatedValueUnwrapper<Optional<?>> {

    private final TypeResolver resolver = new TypeResolver();

    @Override
    public Object handleValidatedValue(final Optional<?> optional) {
        return optional.orNull();
    }

    @Override
    public Type getValidatedValueType(final Type type) {
        final ResolvedType resolvedType = resolver.resolve(type);
        return resolvedType.typeParametersFor(Optional.class).get(0).getErasedType();
    }
}
