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
        if (!OptionalInt.class.equals(rawType)) {
            return null;
        }
        final String defaultValue = DefaultValueUtils.getDefaultValue(annotations);
        return (ParamConverter<T>) (defaultValue == null ? paramConverter : new OptionalIntParamConverter(defaultValue));
    }

    public static class OptionalIntParamConverter implements ParamConverter<OptionalInt> {

        @Nullable
        private final String defaultValue;

        public OptionalIntParamConverter() {
            this(null);
        }

        public OptionalIntParamConverter(@Nullable String defaultValue) {
            this.defaultValue = defaultValue;
        }

        @SuppressWarnings("OptionalAssignedToNull")
        @Override
        @Nullable
        public OptionalInt fromString(final String value) {
            try {
                final int i = Integer.parseInt(value);
                return OptionalInt.of(i);
            } catch (NumberFormatException e) {
                if (defaultValue != null) {
                    // If an invalid default value is specified, we want to fail fast.
                    // This is the same behavior as DropWizard 1.3.x and matches Jersey's handling of @DefaultValue for Integer.
                    if (defaultValue.equals(value)) {
                        throw e;
                    }
                    // In order to fall back to use a default value for an empty query param, we must return null here.
                    // This preserves backwards compatibility with DropWizard 1.3.x handling of empty query params.
                    if (value.isEmpty()) {
                        return null;
                    }
                }
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
