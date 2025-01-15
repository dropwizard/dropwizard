package io.dropwizard.jersey.params;

import io.dropwizard.jersey.DefaultValueUtils;
import io.dropwizard.jersey.validation.JerseyParameterNameProvider;
import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import org.jspecify.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.Type;

/**
 * Provides converters to Jersey for Dropwizard's *Param classes.
 *
 * <p>When a param class is used as a resource parameter this converter will instantiate the parameter class with the
 * value provided and the name of the parameter, so if value parsing fails the parameter name can be used in the error
 * message. If the param class does not have a two-string constructor this provider will return null, causing jersey
 * to use the single-string constructor for the parameter type as it normally would.</p>
 */
public class AbstractParamConverterProvider implements ParamConverterProvider {
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
            final String defaultValue = DefaultValueUtils.getDefaultValue(annotations);
            return new AbstractParamConverter<>(constructor, parameterName, defaultValue);
        }
        return null;
    }

}
