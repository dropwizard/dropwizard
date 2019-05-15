package io.dropwizard.jersey.validation;

import io.dropwizard.jersey.errors.ErrorMessage;
import io.dropwizard.util.Enums;
import io.dropwizard.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nullable;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.Provider;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * Provides converters to jersey for enums used as resource parameters.
 *
 * <p>By default jersey will return a 404 if a resource parameter of an enum type cannot be converted. This class
 * provides converters for all enum types used as resource parameters that provide better error handling. If an
 * invalid value is provided for the parameter a {@code 400 Bad Request} is returned and the error message will
 * include the parameter name and a list of valid values.</p>
 */
@Provider
public class FuzzyEnumParamConverter<T> implements ParamConverter<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(FuzzyEnumParamConverter.class);

    private final Class<T> rawType;
    private final Method fromStringMethod;
    private final Enum<?>[] constants;
    private final String parameterName;

    FuzzyEnumParamConverter(Class<T> rawType,
                            Method fromStringMethod,
                            Enum<?>[] constants,
                            String parameterName) {
        this.rawType = rawType;
        this.fromStringMethod = fromStringMethod;
        this.constants = constants;
        this.parameterName = parameterName;
    }

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
                    @SuppressWarnings("unchecked") final T returnValue = (T) constant;
                    return returnValue;
                }
                final String errMsg = String.format("%s is not a valid %s", parameterName, rawType.getSimpleName());
                throw new WebApplicationException(getErrorResponse(errMsg));
            } catch (IllegalAccessException e) {
                final String errMsg = String.format("Not permitted to call fromString on %s", rawType.getSimpleName());
                LOGGER.debug(errMsg, e);
                throw new WebApplicationException(getErrorResponse(errMsg));
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof WebApplicationException) {
                    throw (WebApplicationException) e.getCause();
                }
                final String errMsg = String.format("Failed to convert %s to %s", parameterName, rawType.getSimpleName());
                LOGGER.debug(errMsg, e);
                throw new WebApplicationException(getErrorResponse(errMsg));
            }
        }

        Object constant = Enums.fromStringFuzzy(value, constants);

        // return if a value is found
        if (constant != null) {
            @SuppressWarnings("unchecked") final T returnValue = (T) constant;
            return returnValue;
        }

        final String constantsList = Arrays.stream(constants)
                .map(Enum::toString)
                .collect(Collectors.joining(", "));
        final String errMsg = String.format("%s must be one of [%s]", parameterName, constantsList);
        throw new WebApplicationException(getErrorResponse(errMsg));
    }

    @Override
    public String toString(T value) {
        return value.toString();
    }

    private Response getErrorResponse(String message) {
        return Response
                .status(400)
                .entity(new ErrorMessage(400, message))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .build();
    }
}
