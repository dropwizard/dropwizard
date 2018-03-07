package io.dropwizard.validation.valuehandling;

import javax.validation.valueextraction.ExtractedValue;
import javax.validation.valueextraction.UnwrapByDefault;
//import org.hibernate.validator.spi.valuehandling.ValidatedValueUnwrapper;
import javax.validation.valueextraction.ValueExtractor;

import com.fasterxml.classmate.TypeResolver;
import com.google.common.base.Optional;

/**
 * A {@link ValidatedValueUnwrapper} for Guava's {@link Optional}.
 *
 * Extracts the value contained by the {@link Optional} for validation, or produces {@code null}.
 */
//public class GuavaOptionalValidatedValueUnwrapper extends ValidatedValueUnwrapper<Optional<?>> {
@UnwrapByDefault
public class GuavaOptionalValidatedValueExtractor implements ValueExtractor<Optional<@ExtractedValue ?>> {


    private final TypeResolver resolver = new TypeResolver();

    /*
    @Override
    public Object handleValidatedValue(final Optional<?> optional) {
        return optional.orNull();
    }

    @Override
    public Type getValidatedValueType(final Type type) {
        final ResolvedType resolvedType = resolver.resolve(type);
        return resolvedType.typeParametersFor(Optional.class).get(0).getErasedType();
    }
    */

	@Override
	public void extractValues(Optional<?> originalValue, ValueReceiver receiver) {
		receiver.value( null, originalValue.orNull() );
		
	}
}
