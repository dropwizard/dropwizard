package com.yammer.dropwizard.setup;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.cfg.HandlerInstantiator;
import com.fasterxml.jackson.databind.introspect.VisibilityChecker;
import com.fasterxml.jackson.databind.jsontype.SubtypeResolver;
import com.fasterxml.jackson.databind.jsontype.TypeResolverBuilder;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.ser.DefaultSerializerProvider;
import com.fasterxml.jackson.databind.ser.FilterProvider;
import com.fasterxml.jackson.databind.ser.SerializerFactory;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.yammer.dropwizard.json.ObjectMapperFactory;

import java.text.DateFormat;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

public class JsonEnvironment {
    private final ObjectMapperFactory objectMapperFactory;

    public JsonEnvironment(ObjectMapperFactory objectMapperFactory) {
        this.objectMapperFactory = objectMapperFactory;
    }

    public AnnotationIntrospector getAnnotationIntrospector() {
        return objectMapperFactory.getAnnotationIntrospector();
    }

    public void enable(JsonFactory.Feature... features) {
        objectMapperFactory.enable(features);
    }

    public void setHandlerInstantiator(HandlerInstantiator handlerInstantiator) {
        objectMapperFactory.setHandlerInstantiator(handlerInstantiator);
    }

    public void setAnnotationIntrospector(AnnotationIntrospector annotationIntrospector) {
        objectMapperFactory.setAnnotationIntrospector(annotationIntrospector);
    }

    public boolean isEnabled(DeserializationFeature feature) {
        return objectMapperFactory.isEnabled(feature);
    }

    public void disable(DeserializationFeature... features) {
        objectMapperFactory.disable(features);
    }

    public void enable(JsonGenerator.Feature... features) {
        objectMapperFactory.enable(features);
    }

    public void setMixinAnnotations(Map<Class<?>, Class<?>> mixinAnnotations) {
        objectMapperFactory.setMixinAnnotations(mixinAnnotations);
    }

    public FilterProvider getFilters() {
        return objectMapperFactory.getFilters();
    }

    public void disable(JsonGenerator.Feature... features) {
        objectMapperFactory.disable(features);
    }

    public VisibilityChecker<?> getVisibilityChecker() {
        return objectMapperFactory.getVisibilityChecker();
    }

    public Map<Class<?>, Class<?>> getMixinAnnotations() {
        return objectMapperFactory.getMixinAnnotations();
    }

    public JsonInclude.Include getSerializationInclusion() {
        return objectMapperFactory.getSerializationInclusion();
    }

    public void setInjectableValues(InjectableValues injectableValues) {
        objectMapperFactory.setInjectableValues(injectableValues);
    }

    public TypeResolverBuilder<?> getDefaultTyping() {
        return objectMapperFactory.getDefaultTyping();
    }

    public void disable(MapperFeature... features) {
        objectMapperFactory.disable(features);
    }

    public void setDateFormat(DateFormat dateFormat) {
        objectMapperFactory.setDateFormat(dateFormat);
    }

    public void setVisibilityRules(PropertyAccessor accessor, JsonAutoDetect.Visibility visibility) {
        objectMapperFactory.setVisibilityRules(accessor, visibility);
    }

    public void setNodeFactory(JsonNodeFactory nodeFactory) {
        objectMapperFactory.setNodeFactory(nodeFactory);
    }

    public void setDefaultTyping(TypeResolverBuilder<?> defaultTyping) {
        objectMapperFactory.setDefaultTyping(defaultTyping);
    }

    public boolean isEnabled(JsonFactory.Feature feature) {
        return objectMapperFactory.isEnabled(feature);
    }

    public PropertyNamingStrategy getPropertyNamingStrategy() {
        return objectMapperFactory.getPropertyNamingStrategy();
    }

    public void setSerializerProvider(DefaultSerializerProvider serializerProvider) {
        objectMapperFactory.setSerializerProvider(serializerProvider);
    }

    public SubtypeResolver getSubtypeResolver() {
        return objectMapperFactory.getSubtypeResolver();
    }

    public DefaultSerializerProvider getSerializerProvider() {
        return objectMapperFactory.getSerializerProvider();
    }

    public void registerModule(Module module) {
        objectMapperFactory.registerModule(module);
    }

    public boolean isEnabled(JsonGenerator.Feature feature) {
        return objectMapperFactory.isEnabled(feature);
    }

    public void setTypeFactory(TypeFactory typeFactory) {
        objectMapperFactory.setTypeFactory(typeFactory);
    }

    public void setFilters(FilterProvider filters) {
        objectMapperFactory.setFilters(filters);
    }

    public void setVisibilityChecker(VisibilityChecker<?> visibilityChecker) {
        objectMapperFactory.setVisibilityChecker(visibilityChecker);
    }

    public boolean isEnabled(SerializationFeature feature) {
        return objectMapperFactory.isEnabled(feature);
    }

    public boolean isEnabled(JsonParser.Feature feature) {
        return objectMapperFactory.isEnabled(feature);
    }

    public void enable(MapperFeature... features) {
        objectMapperFactory.enable(features);
    }

    public boolean isEnabled(MapperFeature feature) {
        return objectMapperFactory.isEnabled(feature);
    }

    public HandlerInstantiator getHandlerInstantiator() {
        return objectMapperFactory.getHandlerInstantiator();
    }

    public void enable(SerializationFeature... features) {
        objectMapperFactory.enable(features);
    }

    public JsonAutoDetect.Visibility getVisibility(PropertyAccessor accessor) {
        return objectMapperFactory.getVisibility(accessor);
    }

    public InjectableValues getInjectableValues() {
        return objectMapperFactory.getInjectableValues();
    }

    public void disable(SerializationFeature... features) {
        objectMapperFactory.disable(features);
    }

    public Locale getLocale() {
        return objectMapperFactory.getLocale();
    }

    public void setSubtypeResolver(SubtypeResolver subtypeResolver) {
        objectMapperFactory.setSubtypeResolver(subtypeResolver);
    }

    public void setLocale(Locale locale) {
        objectMapperFactory.setLocale(locale);
    }

    public void disable(JsonParser.Feature... features) {
        objectMapperFactory.disable(features);
    }

    public DateFormat getDateFormat() {
        return objectMapperFactory.getDateFormat();
    }

    public TypeFactory getTypeFactory() {
        return objectMapperFactory.getTypeFactory();
    }

    public JsonNodeFactory getNodeFactory() {
        return objectMapperFactory.getNodeFactory();
    }

    public void setTimeZone(TimeZone timeZone) {
        objectMapperFactory.setTimeZone(timeZone);
    }

    public void setSerializerFactory(SerializerFactory serializerFactory) {
        objectMapperFactory.setSerializerFactory(serializerFactory);
    }

    public void enable(JsonParser.Feature... features) {
        objectMapperFactory.enable(features);
    }

    public TimeZone getTimeZone() {
        return objectMapperFactory.getTimeZone();
    }

    public void setSerializationInclusion(JsonInclude.Include serializationInclusion) {
        objectMapperFactory.setSerializationInclusion(serializationInclusion);
    }

    public void setPropertyNamingStrategy(PropertyNamingStrategy propertyNamingStrategy) {
        objectMapperFactory.setPropertyNamingStrategy(propertyNamingStrategy);
    }

    public void disable(JsonFactory.Feature... features) {
        objectMapperFactory.disable(features);
    }

    public void enable(DeserializationFeature... features) {
        objectMapperFactory.enable(features);
    }

    public SerializerFactory getSerializerFactory() {
        return objectMapperFactory.getSerializerFactory();
    }

    public ObjectMapper buildObjectMapper() {
        return objectMapperFactory.build();
    }
}
