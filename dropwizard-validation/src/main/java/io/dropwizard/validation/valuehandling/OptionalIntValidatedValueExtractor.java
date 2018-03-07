package io.dropwizard.validation.valuehandling;

//import org.hibernate.validator.spi.valuehandling.ValidatedValueUnwrapper;

import javax.annotation.Nullable;
import javax.validation.valueextraction.ExtractedValue;
import javax.validation.valueextraction.UnwrapByDefault;
import javax.validation.valueextraction.ValueExtractor;

import java.lang.reflect.Type;
import java.util.OptionalInt;

/**
 * A {@link ValidatedValueUnwrapper} for {@link OptionalInt}.
 *
 * Extracts the value contained by the {@link OptionalInt} for validation, or
 * produces {@code null}.
 */
@UnwrapByDefault
public class OptionalIntValidatedValueExtractor
		implements ValueExtractor<@ExtractedValue(type = Integer.class) OptionalInt> {

	@Override
	public void extractValues(OptionalInt originalValue, ValueReceiver receiver) {
		receiver.value(null, originalValue.isPresent() ? originalValue.getAsInt() : null);
	}

	/*
	 * @Override
	 * 
	 * @Nullable public Object handleValidatedValue(final OptionalInt optional) {
	 * return optional.isPresent() ? optional.getAsInt() : null; }
	 * 
	 * @Override public Type getValidatedValueType(final Type type) { return
	 * Integer.class; }
	 */
}
