package io.dropwizard.jersey.validation;

import io.dropwizard.util.Enums;
import jakarta.ws.rs.BadRequestException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.Provider;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
 *
 * @since 2.0
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
        if (value == null || value.isEmpty()) {
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
                throw new BadRequestException(errMsg);
            } catch (IllegalAccessException e) {
                LOGGER.debug("Not permitted to call fromString on {}", rawType.getSimpleName(), e);
                throw new BadRequestException(
                    "Not permitted to call fromString on " + rawType.getSimpleName());
            } catch (InvocationTargetException e) {
                if (e.getCause() instanceof WebApplicationException webApplicationException) {
                    throw webApplicationException;
                }
                LOGGER.debug("Failed to convert {} to {}", parameterName, rawType.getSimpleName(), e);
                throw new BadRequestException(
                    "Failed to convert " + parameterName + " to " + rawType.getSimpleName());
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
        throw new BadRequestException(errMsg);
    }

    @Override
    public String toString(T value) {
        return value.toString();
    }
}
