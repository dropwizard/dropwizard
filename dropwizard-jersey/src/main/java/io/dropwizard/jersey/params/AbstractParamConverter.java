package io.dropwizard.jersey.params;

import io.dropwizard.util.Strings;
import org.glassfish.jersey.internal.inject.ExtractorException;
import org.glassfish.jersey.server.internal.LocalizationMessages;

import javax.annotation.Nullable;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.ParamConverter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Converter to Jersey for Dropwizard's *Param classes.
 *
 * <p>When a param class is used as a resource parameter this converter will instantiate the parameter class with the
 * value provided and the name of the parameter, so if value parsing fails the parameter name can be used in the error
 * message. If the param class does not have a two-string constructor this provider will return null, causing jersey
 * to use the single-string constructor for the parameter type as it normally would.</p>
 *
 * @since 2.0
 */
public class AbstractParamConverter<T> implements ParamConverter<T> {
    private final Constructor<T> constructor;
    private final String parameterName;
    @Nullable
    private final String defaultValue;

    public AbstractParamConverter(Constructor<T> constructor, String parameterName, @Nullable String defaultValue) {
        this.constructor = constructor;
        this.parameterName = parameterName;
        this.defaultValue = defaultValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @Nullable
    public T fromString(String value) {
        try {
            final String defaultedValue = Strings.isNullOrEmpty(value) && defaultValue != null ? defaultValue : value;
            return constructor.newInstance(defaultedValue, parameterName);
        } catch (InvocationTargetException ex) {
            final Throwable cause = ex.getCause();
            if (cause instanceof WebApplicationException) {
                throw (WebApplicationException) cause;
            } else {
                throw new ExtractorException(cause);
            }
        } catch (final Exception ex) {
            throw new ProcessingException(ex);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString(T value) throws IllegalArgumentException {
        if (value == null) {
            throw new IllegalArgumentException(LocalizationMessages.METHOD_PARAMETER_CANNOT_BE_NULL("value"));
        }
        return value.toString();
    }

}
