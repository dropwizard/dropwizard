package io.dropwizard.jersey.optional;

import javax.inject.Singleton;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.OptionalLong;

import static com.google.common.base.Preconditions.checkArgument;

@Singleton
public class OptionalLongParamConverterProvider implements ParamConverterProvider {
    private OptionalLongParamConverter paramConverter = new OptionalLongParamConverter();

    /**
     * {@inheritDoc}
     */
    @Override
    @SuppressWarnings("unchecked")
    public <T> ParamConverter<T> getConverter(final Class<T> rawType, final Type genericType,
                                              final Annotation[] annotations) {
        return OptionalLong.class.equals(rawType) ? (ParamConverter<T>) paramConverter : null;
    }

    public static class OptionalLongParamConverter implements ParamConverter<OptionalLong> {
        @Override
        public OptionalLong fromString(final String value) {
            if (value == null) {
                return OptionalLong.empty();
            }

            try {
                return OptionalLong.of(Long.parseLong(value));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException(e);
            }
        }

        @Override
        public String toString(final OptionalLong value) {
            checkArgument(value != null);
            return value.isPresent() ? Long.toString(value.getAsLong()) : "";
        }
    }
}
