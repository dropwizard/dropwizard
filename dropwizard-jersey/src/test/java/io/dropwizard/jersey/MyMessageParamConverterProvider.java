package io.dropwizard.jersey;

import jakarta.ws.rs.ext.ParamConverter;
import jakarta.ws.rs.ext.ParamConverterProvider;
import jakarta.ws.rs.ext.Provider;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Type;

@Provider
public class MyMessageParamConverterProvider implements ParamConverterProvider {

    @Override
    @SuppressWarnings("unchecked")
    @Nullable
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type genericType, Annotation[] annotations) {
        if (genericType.equals(MyMessage.class)) {
            return (ParamConverter<T>) new MyMessageParamConverter();
        }
        return null;
    }

    private static class MyMessageParamConverter implements ParamConverter<MyMessage> {

        @Override
        public MyMessage fromString(String value) {
            return new MyMessage(value);
        }

        @Override
        public String toString(MyMessage value) {
            return value.getMessage();
        }
    }
}
