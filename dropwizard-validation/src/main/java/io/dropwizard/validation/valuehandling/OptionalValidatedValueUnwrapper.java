package io.dropwizard.validation.valuehandling;

import com.fasterxml.classmate.ResolvedType;
import com.fasterxml.classmate.TypeResolver;
import org.hibernate.validator.spi.valuehandling.ValidatedValueUnwrapper;

import java.lang.reflect.Type;
import java.util.Optional;

/**
 * A {@link ValidatedValueUnwrapper} for {@link Optional}.
 *
 * Extracts the value contained by the {@link Optional} for validation, or produces {@code null}.
 */
public class OptionalValidatedValueUnwrapper extends ValidatedValueUnwrapper<Optional<?>> {

    private final TypeResolver resolver = new TypeResolver();

    @Override
    public Object handleValidatedValue(final Optional<?> optional) {
        return optional.orElse(null);
    }

    @Override
    public Type getValidatedValueType(final Type type) {
        final ResolvedType resolvedType = resolver.resolve(type);
        return resolvedType.typeParametersFor(Optional.class).get(0).getErasedType();
    }
}
