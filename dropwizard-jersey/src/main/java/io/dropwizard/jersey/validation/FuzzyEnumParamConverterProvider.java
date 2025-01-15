package io.dropwizard.jersey.validation;

import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;
import org.jspecify.annotations.Nullable;
import org.glassfish.jersey.internal.util.ReflectionHelper;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.security.AccessController;

import static io.dropwizard.jersey.validation.JerseyParameterNameProvider.getParameterNameFromAnnotations;

/**
 * Provides converters to Jersey for enums used as resource parameters.
 *
 * <p>By default jersey will return a 404 if a resource parameter of an enum type cannot be converted. This class
 * provides converters for all enum types used as resource parameters that provide better error handling. If an
 * invalid value is provided for the parameter a {@code 400 Bad Request} is returned and the error message will
 * include the parameter name and a list of valid values.</p>
 */
@Provider
public class FuzzyEnumParamConverterProvider implements ParamConverterProvider {
    @Override
    @Nullable
    public <T> ParamConverter<T> getConverter(Class<T> rawType, @Nullable Type genericType, Annotation[] annotations) {
        if (!rawType.isEnum()) {
            return null;
        }

        @SuppressWarnings("unchecked") final Class<Enum<?>> type = (Class<Enum<?>>) rawType;
        final Enum<?>[] constants = type.getEnumConstants();
        final String parameterName = getParameterNameFromAnnotations(annotations).orElse("Parameter");
        final Method fromStringMethod = AccessController.doPrivileged(ReflectionHelper.getFromStringStringMethodPA(rawType));

        return new FuzzyEnumParamConverter<>(rawType, fromStringMethod, constants, parameterName);
    }

}
