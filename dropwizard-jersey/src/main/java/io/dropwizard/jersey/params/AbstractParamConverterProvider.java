package io.dropwizard.jersey.params;

import io.dropwizard.jersey.validation.JerseyParameterNameProvider;
import io.dropwizard.util.Strings;
import org.glassfish.jersey.internal.inject.ExtractorException;
import org.glassfish.jersey.server.internal.LocalizationMessages;

import javax.annotation.Nullable;
import javax.ws.rs.ProcessingException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

/**
 * Provides converters to jersey for dropwizard's *Param classes.
 *
 * <p>When a param class is used as a resource parameter this converter will instantiate the parameter class with the
 * value provided and the name of the parameter, so if value parsing fails the parameter name can be used in the error
 * message. If the param class does not have a two-string constructor this provider will return null, causing jersey
 * to use the single-string constructor for the parameter type as it normally would.</p>
 */
public class AbstractParamConverterProvider implements ParamConverterProvider {

    public AbstractParamConverterProvider() {
    }

    @Override
    @Nullable
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        if (AbstractParam.class.isAssignableFrom(rawType)) {
            final String parameterName = JerseyParameterNameProvider.getParameterNameFromAnnotations(annotations).orElse("Parameter");
            final Constructor<T> constructor;
            try {
                constructor = rawType.getConstructor(String.class, String.class);
            } catch (NoSuchMethodException ignored) {
                // The Param class did not have a (String, String) constructor. We return null,
                // leaving Jersey to handle these parameters as it normally would.
                return null;
            }
            return new ParamConverter<T>() {
                @Override
                @Nullable
                public T fromString(String value) {
                    if (rawType != NonEmptyStringParam.class && Strings.isNullOrEmpty(value)) {
                        return null;
                    }
                    try {
                        return _fromString(value);
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

                protected T _fromString(String value) throws Exception {
                    return constructor.newInstance(value, parameterName);
                }

                @Override
                public String toString(T value) throws IllegalArgumentException {
                    if (value == null) {
                        throw new IllegalArgumentException(LocalizationMessages.METHOD_PARAMETER_CANNOT_BE_NULL("value"));
                    }
                    return value.toString();
                }

            };
        }
        return null;
    }

}
