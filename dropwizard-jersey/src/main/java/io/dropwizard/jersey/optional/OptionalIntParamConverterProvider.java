package io.dropwizard.jersey.optional;

import javax.annotation.Nullable;
import javax.inject.Singleton;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.OptionalInt;

@Singleton
public class OptionalIntParamConverterProvider implements ParamConverterProvider {
    private final OptionalIntParamConverter paramConverter = new OptionalIntParamConverter();

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    @Nullable
    public <T> ParamConverter<T> getConverter(final Class<T> rawType, final Type genericType,
                                              final Annotation[] annotations) {
        return OptionalInt.class.equals(rawType) ? (ParamConverter<T>) paramConverter : null;
    }

    public static class OptionalIntParamConverter implements ParamConverter<OptionalInt> {
        @Override
        public OptionalInt fromString(final String value) {
            try {
                final int i = Integer.parseInt(value);
                return OptionalInt.of(i);
            } catch (NumberFormatException e) {
                return OptionalInt.empty();
            }
        }

        @Override
        public String toString(final OptionalInt value) {
            if (value == null) {
                throw new IllegalArgumentException("value must not be null");
            }
            return value.isPresent() ? Integer.toString(value.getAsInt()) : "";
        }
    }
}
