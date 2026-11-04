package io.dropwizard.jersey.params;

import io.dropwizard.jersey.validation.JerseyParameterNameProvider;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;
import org.jspecify.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;
import java.util.UUID;

/**
 * Provides a {@link ParamConverter} for {@link UUID} resource parameters with consistent
 * {@code 400 Bad Request} handling for empty and invalid values.
 *
 * @see UUIDParamConverter
 * @see <a href="https://github.com/dropwizard/dropwizard/issues/3435">dropwizard/dropwizard#3435</a>
 * @since 5.0.3
 */
@Provider
public class UUIDParamConverterProvider implements ParamConverterProvider {
    @Override
    @Nullable
    public <T> ParamConverter<T> getConverter(Class<T> rawType, @Nullable Type genericType, Annotation[] annotations) {
        if (!UUID.class.equals(rawType)) {
            return null;
        }

        final String parameterName = JerseyParameterNameProvider.getParameterNameFromAnnotations(annotations)
                .orElse("Parameter");
        @SuppressWarnings("unchecked") final ParamConverter<T> converter =
                (ParamConverter<T>) new UUIDParamConverter(parameterName);
        return converter;
    }
}
