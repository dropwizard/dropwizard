package io.dropwizard.jersey.optional;

import javax.inject.Singleton;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.OptionalDouble;

import static com.google.common.base.Preconditions.checkArgument;

@Singleton
public class OptionalDoubleParamConverterProvider implements ParamConverterProvider {
    private final OptionalDoubleParamConverter paramConverter = new OptionalDoubleParamConverter();

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> ParamConverter<T> getConverter(final Class<T> rawType, final Type genericType,
                                              final Annotation[] annotations) {
        return OptionalDouble.class.equals(rawType) ? (ParamConverter<T>) paramConverter : null;
    }

    public static class OptionalDoubleParamConverter implements ParamConverter<OptionalDouble> {
        @Override
        public OptionalDouble fromString(final String value) {
            if (value == null) {
                return OptionalDouble.empty();
            }

            try {
                return OptionalDouble.of(Double.parseDouble(value));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(e);
            }
        }

        @Override
        public String toString(final OptionalDouble value) {
            checkArgument(value != null);
            return value.isPresent() ? Double.toString(value.getAsDouble()) : "";
        }
    }
}
