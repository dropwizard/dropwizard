package io.dropwizard.jersey.validation;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import io.dropwizard.jersey.errors.ErrorMessage;
import io.dropwizard.util.Enums;
import org.glassfish.jersey.internal.util.ReflectionHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.security.AccessController;

import static io.dropwizard.jersey.validation.JerseyParameterNameProvider.getParameterNameFromAnnotations;

/**
 * Provides converters to jersey for enums used as resource parameters.
 *
 * <p>By default jersey will return a 404 if a resource parameter of an enum type cannot be converted. This class
 * provides converters for all enum types used as resource parameters that provide better error handling. If an
 * invalid value is provided for the parameter a {@code 400 Bad Request} is returned and the error message will
 * include the parameter name and a list of valid values.</p>
 */
@SuppressWarnings("unchecked")
@Provider
public class FuzzyEnumParamConverterProvider implements ParamConverterProvider {
    private static final Logger LOGGER = LoggerFactory.getLogger(ParamConverterProvider.class);

    private final static Joiner JOINER = Joiner.on(", ");

    @Override
    @Nullable
    public <T> ParamConverter<T> getConverter(Class<T> rawType, @Nullable Type genericType, Annotation[] annotations) {
        if (!rawType.isEnum()) {
            return null;
        }

        final Class<Enum<?>> type = (Class<Enum<?>>) rawType;
        final Enum<?>[] constants = type.getEnumConstants();
        final String parameterName = getParameterNameFromAnnotations(annotations).orElse("Parameter");
        Method fromStringMethod = AccessController.doPrivileged(ReflectionHelper.getFromStringStringMethodPA(rawType));

        return new ParamConverter<T>() {
            @Override
            @Nullable
            public T fromString(String value) {
                if (Strings.isNullOrEmpty(value)) {
                    return null;
                }

                if (fromStringMethod != null) {
                    try {
                        Object constant = fromStringMethod.invoke(null, value);
                        // return if a value is found
                        if (constant != null) {
                            return (T) constant;
                        }
                        final String errMsg =
                            String.format("%s is not a valid %s", parameterName, rawType.getSimpleName());
                        throw new WebApplicationException(getErrorResponse(errMsg));
                    } catch (IllegalAccessException e) {
                        final String errMsg =
                            String.format("Not permitted to call fromString on %s", rawType.getSimpleName());
                        LOGGER.debug(errMsg, e);
                        throw new WebApplicationException(getErrorResponse(errMsg));
                    } catch (InvocationTargetException e) {
                        if (e.getCause() instanceof WebApplicationException) {
                            throw (WebApplicationException) e.getCause();
                        }
                        final String errMsg =
                            String.format("Failed to convert %s to %s", parameterName, rawType.getSimpleName());
                        LOGGER.debug(errMsg, e);
                        throw new WebApplicationException(getErrorResponse(errMsg));
                    }
                }

                Object constant = Enums.fromStringFuzzy(value, constants);

                // return if a value is found
                if (constant != null) {
                    return (T) constant;
                }

                final String errMsg = String.format("%s must be one of [%s]", parameterName, JOINER.join(constants));
                throw new WebApplicationException(getErrorResponse(errMsg));
            }

            @Override
            public String toString(T value) {
                return value.toString();
            }

            protected Response getErrorResponse(String message) {
                return Response
                    .status(400)
                    .entity(new ErrorMessage(400, message))
                    .type(MediaType.APPLICATION_JSON_TYPE)
                    .build();
            }
        };
    }

}
