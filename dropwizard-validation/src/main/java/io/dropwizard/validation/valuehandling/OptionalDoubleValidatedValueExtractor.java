package io.dropwizard.validation.valuehandling;

//import org.hibernate.validator.spi.valuehandling.ValidatedValueUnwrapper;

import javax.annotation.Nullable;
import javax.validation.valueextraction.ExtractedValue;
import javax.validation.valueextraction.UnwrapByDefault;
import javax.validation.valueextraction.ValueExtractor;
import javax.validation.valueextraction.ValueExtractor.ValueReceiver;

import java.lang.reflect.Type;
import java.util.OptionalDouble;
import java.util.OptionalInt;
import java.util.OptionalLong;

/**
 * A {@link ValidatedValueUnwrapper} for {@link OptionalDouble}.
 *
 * Extracts the value contained by the {@link OptionalDouble} for validation, or
 * produces {@code null}.
 */
@UnwrapByDefault
public class OptionalDoubleValidatedValueExtractor
		implements ValueExtractor<@ExtractedValue(type = Long.class) OptionalDouble> {

	@Override
	public void extractValues(OptionalDouble originalValue, ValueReceiver receiver) {
		receiver.value(null, originalValue.isPresent() ? originalValue.getAsDouble() : null);
	}
	/*
	 * @Override
	 * 
	 * @Nullable public Object handleValidatedValue(final OptionalDouble optional) {
	 * return optional.isPresent() ? optional.getAsDouble() : null; }
	 * 
	 * @Override public Type getValidatedValueType(final Type type) { return
	 * Double.class; }
	 */
}
