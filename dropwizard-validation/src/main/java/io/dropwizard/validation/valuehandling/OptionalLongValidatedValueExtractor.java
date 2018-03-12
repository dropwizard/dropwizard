package io.dropwizard.validation.valuehandling;

//import org.hibernate.validator.spi.valuehandling.ValidatedValueUnwrapper;

import javax.annotation.Nullable;
import javax.validation.valueextraction.ExtractedValue;
import javax.validation.valueextraction.UnwrapByDefault;
import javax.validation.valueextraction.ValueExtractor;
import javax.validation.valueextraction.ValueExtractor.ValueReceiver;

import java.lang.reflect.Type;
import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * A {@link ValidatedValueUnwrapper} for {@link OptionalLong}.
 *
 * Extracts the value contained by the {@link OptionalLong} for validation, or
 * produces {@code null}.
 */
@UnwrapByDefault
public class OptionalLongValidatedValueExtractor
		implements ValueExtractor<@ExtractedValue(type = Long.class) OptionalLong> {

	@Override
	public void extractValues(OptionalLong originalValue, ValueReceiver receiver) {
		receiver.value(null, originalValue.isPresent() ? originalValue.getAsLong() : null);
	}

	/*
	 * @Override
	 * 
	 * @Nullable public Object handleValidatedValue(final OptionalLong optional) {
	 * return optional.isPresent() ? optional.getAsLong() : null; }
	 * 
	 * @Override public Type getValidatedValueType(final Type type) { return
	 * Long.class; }
	 */
}
