/*
 * Copyright (c) 2016. Sense Labs, Inc. All Rights Reserved
 */

package io.dropwizard.jersey.validation;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Sets;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.jersey.errors.ErrorMessage;
import org.glassfish.jersey.server.model.Resource;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ParamConverter;
import javax.ws.rs.ext.ParamConverterProvider;
import javax.ws.rs.ext.Provider;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.EnumSet;
import java.util.Set;

@Provider
public class ValidatingEnumParamConverterProvider<C extends Enum<C>> implements ParamConverterProvider {

    private final DropwizardResourceConfig resourceConfig;
    private Set<Class<C>> enumParameterClasses = null;

    public ValidatingEnumParamConverterProvider(DropwizardResourceConfig resourceConfig) {
        this.resourceConfig = resourceConfig;
    }

    private Set<Class<C>> findEnumsUsedAsParameters(Set<Class<?>> allClasses) {
        Set<Class<C>> foundEnums = Sets.newHashSet();
        allClasses.stream()
            .filter(c -> !c.isInterface() && Resource.from(c) != null)
            .forEach(clazz -> {
                for (Method method : clazz.getDeclaredMethods()) {
                    scanMethodForEnums(method, foundEnums);
                }
            }
        );
        return foundEnums;
    }

    @SuppressWarnings("unchecked")
    private void scanMethodForEnums(Method method, Set<Class<C>> foundEnums) {
        Set<Class<? extends Annotation>> methodAnnotations = Sets.newHashSet(
            GET.class, POST.class, PUT.class, DELETE.class, HEAD.class, OPTIONS.class
        );
        for (Annotation annotation : method.getAnnotations()) {
            if (methodAnnotations.contains(annotation.annotationType())) {
                for (Class<?> paramType : method.getParameterTypes()) {
                    if (paramType.isEnum() && !foundEnums.contains(paramType)) {
                        foundEnums.add((Class<C>) paramType);
                    }
                    else if (paramType.equals(BeanParam.class)) {
                        for (Field field : paramType.getDeclaredFields()) {
                            if (field.getType().isEnum() && !foundEnums.contains(field.getType())) {
                                foundEnums.add((Class<C>)field.getType());
                            }
                        }
                    }
                }
                break;
            }
        }
    }

    protected Class<C> handlesClass(Class<?> clazz) {
        if (enumParameterClasses == null) {
            enumParameterClasses = findEnumsUsedAsParameters(resourceConfig.allClasses());
        }
        for (Class<C> c : enumParameterClasses) {
            if (clazz.getName().equals(c.getName())) {
                return c;
            }
        }
        return null;
    }

    @Override
    public <T> ParamConverter<T> getConverter(Class<T> rawType, Type type, Annotation[] annotations) {
        Class<C> clazz = handlesClass(rawType);
        if (clazz != null) {
            String parameterName = JerseyParameterNameProvider.getParameterNameFromAnnotations(annotations).orElse("Parameter");
            return getConverter(clazz, type, annotations, parameterName);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    protected  <T> ParamConverter<T> getConverter(Class<C> rawType, Type type, Annotation[] annotations, String parameterName) {
        return (ParamConverter<T>)new EnumParamConverter<>(rawType, parameterName);
    }

    protected class EnumParamConverter<E extends Enum<E>> implements ParamConverter<E> {

        private final Class<E> clazz;
        private final String parameterName;

        public EnumParamConverter(Class<E> clazz, String parameterName) {
            this.clazz = clazz;
            this.parameterName = parameterName;
        }

        @Override
        @SuppressWarnings("unchecked")
        public E fromString(String value) {
            if (Strings.isNullOrEmpty(value)) {
                return null;
            }
            EnumSet<? extends Enum<?>> set = EnumSet.allOf(clazz);
            for (Object e : set) {
                if (value.equalsIgnoreCase(e.toString())) {
                    return (E) e;
                }
            }
            throw new WebApplicationException(getErrorResponse(String.format("%s must be one of [%s]", parameterName, Joiner.on(", ").join(set))));
        }

        @Override
        public String toString(E value) {
            return value.toString();
        }

        protected Response getErrorResponse(String message) {
            return Response
                .status(400)
                .entity(new ErrorMessage(400, message))
                .type(MediaType.APPLICATION_JSON_TYPE)
                .build();
        }

    }

}
