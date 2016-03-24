package io.dropwizard.jersey.params;

import javax.ws.rs.core.Feature;
import javax.ws.rs.core.FeatureContext;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.Optional;

/**
 * A class for describing how Jersey should serialize a {@link NonEmptyStringParam}. If the
 * parameter was not detected in the response, instead of the resulting value being null, it will
 * evaluate to {@link Optional#empty()}
 */
public class NonEmptyStringParamFeature implements Feature {
    @Override
    public boolean configure(final FeatureContext context) {
        context.register(new ParamConverterProvider() {
            @Override
            public <T> ParamConverter<T> getConverter(final Class<T> rawType,
                                                      final Type genericType,
                                                      final Annotation[] annotations) {
                return (rawType != NonEmptyStringParam.class) ? null : new ParamConverter<T>() {
                    @Override
                    public T fromString(final String value) {
                        return rawType.cast(new NonEmptyStringParam(value));
                    }

                    @Override
                    public String toString(final T value) {
                        return value == null ? null : value.toString();
                    }
                };
            }
        });
        return true;
    }
}
