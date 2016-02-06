package io.dropwizard.jersey.optional;

import javax.inject.Singleton;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.OptionalInt;

import static com.google.common.base.Preconditions.checkArgument;

@Singleton
public class OptionalIntParamConverterProvider implements ParamConverterProvider {
    private final OptionalIntParamConverter paramConverter = new OptionalIntParamConverter();

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> ParamConverter<T> getConverter(final Class<T> rawType, final Type genericType,
                                              final Annotation[] annotations) {
        return OptionalInt.class.equals(rawType) ? (ParamConverter<T>) paramConverter : null;
    }

    public static class OptionalIntParamConverter implements ParamConverter<OptionalInt> {
        @Override
        public OptionalInt fromString(final String value) {
            if (value == null) {
                return OptionalInt.empty();
            }

            try {
                return OptionalInt.of(Integer.parseInt(value));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(e);
            }
        }

        @Override
        public String toString(final OptionalInt value) {
            checkArgument(value != null);
            return value.isPresent() ? Integer.toString(value.getAsInt()) : "";
        }
    }
}
