package io.dropwizard.jersey.optional;

import javax.annotation.Nullable;
import javax.inject.Singleton;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.OptionalDouble;

@Singleton
public class OptionalDoubleParamConverterProvider implements ParamConverterProvider {
    private final OptionalDoubleParamConverter paramConverter = new OptionalDoubleParamConverter();

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    @Nullable
    public <T> ParamConverter<T> getConverter(final Class<T> rawType, final Type genericType,
                                              final Annotation[] annotations) {
        return OptionalDouble.class.equals(rawType) ? (ParamConverter<T>) paramConverter : null;
    }

    public static class OptionalDoubleParamConverter implements ParamConverter<OptionalDouble> {
        @Override
        public OptionalDouble fromString(final String value) {
            try {
                final double d = Double.parseDouble(value);
                return OptionalDouble.of(d);
            } catch (NumberFormatException e) {
                return OptionalDouble.empty();
            }
        }

        @Override
        public String toString(final OptionalDouble value) {
            if (value == null) {
                throw new IllegalArgumentException("value must not be null");
            }
            return value.isPresent() ? Double.toString(value.getAsDouble()) : "";
        }
    }
}
