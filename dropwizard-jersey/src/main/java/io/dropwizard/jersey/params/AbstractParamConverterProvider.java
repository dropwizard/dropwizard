package io.dropwizard.jersey.params;

import com.google.common.base.Strings;
import io.dropwizard.jersey.validation.JerseyParameterNameProvider;

import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Type;

public class AbstractParamConverterProvider implements ParamConverterProvider {

    public AbstractParamConverterProvider() {
    }

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        if (AbstractParam.class.isAssignableFrom(rawType)) {
            final String parameterName = JerseyParameterNameProvider.getParameterNameFromAnnotations(annotations).orElse("Parameter");
            final Constructor<T> constructor;
            try {
                constructor = rawType.getConstructor(String.class, String.class);
            }
            catch (NoSuchMethodException ignored) {
                // The Param class did not have a (String, String) constructor. We return null,
                // leaving Jersey to handle these parameters as it normally would.
                return null;
            }
            return new ParamConverter<T>() {
                @Override
                @SuppressWarnings("unchecked")
                public T fromString(String s) {
                    if (Strings.isNullOrEmpty(s)) {
                        return null;
                    }
                    try {
                        return constructor.newInstance(s, parameterName);
                    }
                    catch (InstantiationException | IllegalAccessException e) {
                        throw new InternalServerErrorException(String.format("Unable to convert parameter %s: %s", parameterName, e.getMessage()));
                    }
                    catch(InvocationTargetException e) {
                        Throwable t = e.getTargetException();
                        if (t instanceof RuntimeException) {
                            throw (RuntimeException)t;
                        }
                        return null;
                    }
                }

                @Override
                public String toString(T t) {
                    return t.toString();
                }
            };
        }
        return null;
    }

}
