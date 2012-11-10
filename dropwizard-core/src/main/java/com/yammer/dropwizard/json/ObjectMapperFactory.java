package com.yammer.dropwizard.json;

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
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.text.DateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * A factory class for {@link ObjectMapper}.
 *
 * <p>By default, ObjectMapperFactory is configured to:</p>
 * <ul>
 *     <li>Allow both C-style line and block comments.</li>
 *     <li>Not fail when encountering unknown properties.</li>
 *     <li>Use {@code snake_case} for property names when encoding and decoding
 *         classes annotated with {@link JsonSnakeCase}.</li>
 *     <li>Support Guava and Logback types.</li>
 * </ul>
 */
@SuppressWarnings("UnusedDeclaration")
public class ObjectMapperFactory {
    private final List<Module> modules;
    private final Map<MapperFeature, Boolean> mapperFeatures;
    private final Map<DeserializationFeature, Boolean> deserializationFeatures;
    private final Map<SerializationFeature, Boolean> serializationFeatures;
    private final Map<JsonGenerator.Feature, Boolean> generatorFeatures;
    private final Map<JsonParser.Feature, Boolean> parserFeatures;
    private final Map<JsonFactory.Feature, Boolean> factoryFeatures;
    private final Map<PropertyAccessor, JsonAutoDetect.Visibility> visibilityRules;

    private AnnotationIntrospector annotationIntrospector;
    private DateFormat dateFormat;
    private PropertyNamingStrategy propertyNamingStrategy;
    private TypeResolverBuilder<?> defaultTyping;
    private FilterProvider filters;
    private HandlerInstantiator handlerInstantiator;
    private InjectableValues injectableValues;
    private Locale locale;
    private Map<Class<?>, Class<?>> mixinAnnotations;
    private JsonNodeFactory nodeFactory;
    private JsonInclude.Include serializationInclusion;
    private SerializerFactory serializerFactory;
    private DefaultSerializerProvider serializerProvider;
    private SubtypeResolver subtypeResolver;
    private TimeZone timeZone;
    private TypeFactory typeFactory;
    private VisibilityChecker<?> visibilityChecker;


    /**
     * Create a new ObjectMapperFactory.
     */
    public ObjectMapperFactory() {
        this.modules = Lists.newArrayList();
        this.mapperFeatures = Maps.newHashMap();
        this.deserializationFeatures = Maps.newHashMap();
        this.serializationFeatures = Maps.newHashMap();
        this.generatorFeatures = Maps.newHashMap();
        this.parserFeatures = Maps.newHashMap();
        this.factoryFeatures = Maps.newHashMap();
        this.visibilityRules = Maps.newLinkedHashMap();

        this.propertyNamingStrategy = AnnotationSensitivePropertyNamingStrategy.INSTANCE;

        enable(JsonParser.Feature.ALLOW_COMMENTS);
        disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        registerModule(new GuavaModule());
        registerModule(new LogbackModule());
        registerModule(new GuavaExtrasModule());
    }

    public AnnotationIntrospector getAnnotationIntrospector() {
        return annotationIntrospector;
    }

    public void setAnnotationIntrospector(AnnotationIntrospector annotationIntrospector) {
        this.annotationIntrospector = annotationIntrospector;
    }

    public DateFormat getDateFormat() {
        return dateFormat;
    }

    public void setDateFormat(DateFormat dateFormat) {
        this.dateFormat = dateFormat;
    }

    public TypeResolverBuilder<?> getDefaultTyping() {
        return defaultTyping;
    }

    public void setDefaultTyping(TypeResolverBuilder<?> defaultTyping) {
        this.defaultTyping = defaultTyping;
    }

    public FilterProvider getFilters() {
        return filters;
    }

    public void setFilters(FilterProvider filters) {
        this.filters = filters;
    }

    public HandlerInstantiator getHandlerInstantiator() {
        return handlerInstantiator;
    }

    public void setHandlerInstantiator(HandlerInstantiator handlerInstantiator) {
        this.handlerInstantiator = handlerInstantiator;
    }

    public InjectableValues getInjectableValues() {
        return injectableValues;
    }

    public void setInjectableValues(InjectableValues injectableValues) {
        this.injectableValues = injectableValues;
    }

    public Locale getLocale() {
        return locale;
    }

    public void setLocale(Locale locale) {
        this.locale = locale;
    }

    public Map<Class<?>, Class<?>> getMixinAnnotations() {
        return mixinAnnotations;
    }

    public void setMixinAnnotations(Map<Class<?>, Class<?>> mixinAnnotations) {
        this.mixinAnnotations = mixinAnnotations;
    }

    public JsonNodeFactory getNodeFactory() {
        return nodeFactory;
    }

    public void setNodeFactory(JsonNodeFactory nodeFactory) {
        this.nodeFactory = nodeFactory;
    }

    public PropertyNamingStrategy getPropertyNamingStrategy() {
        return propertyNamingStrategy;
    }

    public void setPropertyNamingStrategy(PropertyNamingStrategy propertyNamingStrategy) {
        this.propertyNamingStrategy = propertyNamingStrategy;
    }

    public JsonInclude.Include getSerializationInclusion() {
        return serializationInclusion;
    }

    public void setSerializationInclusion(JsonInclude.Include serializationInclusion) {
        this.serializationInclusion = serializationInclusion;
    }

    public SerializerFactory getSerializerFactory() {
        return serializerFactory;
    }

    public void setSerializerFactory(SerializerFactory serializerFactory) {
        this.serializerFactory = serializerFactory;
    }

    public DefaultSerializerProvider getSerializerProvider() {
        return serializerProvider;
    }

    public void setSerializerProvider(DefaultSerializerProvider serializerProvider) {
        this.serializerProvider = serializerProvider;
    }

    public SubtypeResolver getSubtypeResolver() {
        return subtypeResolver;
    }

    public void setSubtypeResolver(SubtypeResolver subtypeResolver) {
        this.subtypeResolver = subtypeResolver;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public TypeFactory getTypeFactory() {
        return typeFactory;
    }

    public void setTypeFactory(TypeFactory typeFactory) {
        this.typeFactory = typeFactory;
    }

    public JsonAutoDetect.Visibility getVisibility(PropertyAccessor accessor) {
        return visibilityRules.get(accessor);
    }

    public void setVisibilityRules(PropertyAccessor accessor, JsonAutoDetect.Visibility visibility) {
        visibilityRules.put(accessor, visibility);
    }

    public VisibilityChecker<?> getVisibilityChecker() {
        return visibilityChecker;
    }

    public void setVisibilityChecker(VisibilityChecker<?> visibilityChecker) {
        this.visibilityChecker = visibilityChecker;
    }

    /**
     * Registers a module that can extend functionality provided by this class; for example, by
     * adding providers for custom serializers and deserializers.
     *
     * @param module Module to register
     * @see ObjectMapper#registerModule(Module)
     */
    public void registerModule(Module module) {
        modules.add(module);
    }

    /**
     * Returns true if the given {@link MapperFeature} is enabled.
     *
     * @param feature a given feature
     * @return {@code true} if {@code feature} is enabled
     * @see ObjectMapper#isEnabled(MapperFeature)
     */
    public boolean isEnabled(MapperFeature feature) {
        final Boolean enabled = mapperFeatures.get(feature);
        if (enabled != null) {
            return enabled;
        }
        return feature.enabledByDefault();
    }

    /**
     * Enables the given {@link MapperFeature}s.
     *
     * @param features a set of features to enable
     * @see ObjectMapper#enable(MapperFeature...)
     */
    public void enable(MapperFeature... features) {
        for (MapperFeature feature : features) {
            mapperFeatures.put(feature, Boolean.TRUE);
        }
    }

    /**
     * Disables the given {@link MapperFeature}s.
     *
     * @param features a set of features to disable
     * @see ObjectMapper#disable(MapperFeature...)
     */
    public void disable(MapperFeature... features) {
        for (MapperFeature feature : features) {
            mapperFeatures.put(feature, Boolean.FALSE);
        }
    }

    /**
     * Returns true if the given {@link DeserializationFeature} is enabled.
     *
     * @param feature a given feature
     * @return {@code true} if {@code feature} is enabled
     * @see ObjectMapper#isEnabled(DeserializationFeature)
     */
    public boolean isEnabled(DeserializationFeature feature) {
        final Boolean enabled = deserializationFeatures.get(feature);
        if (enabled != null) {
            return enabled;
        }
        return feature.enabledByDefault();
    }

    /**
     * Enables the given {@link DeserializationFeature}s.
     *
     * @param features a set of features to enable
     * @see ObjectMapper#enable(DeserializationFeature)
     */
    public void enable(DeserializationFeature... features) {
        for (DeserializationFeature feature : features) {
            deserializationFeatures.put(feature, Boolean.TRUE);
        }
    }

    /**
     * Disables the given {@link DeserializationFeature}s.
     *
     * @param features a set of features to disable
     * @see ObjectMapper#disable(DeserializationFeature)
     */
    public void disable(DeserializationFeature... features) {
        for (DeserializationFeature feature : features) {
            deserializationFeatures.put(feature, Boolean.FALSE);
        }
    }

    /**
     * Returns true if the given {@link SerializationFeature} is enabled.
     *
     * @param feature a given feature
     * @return {@code true} if {@code feature} is enabled
     * @see ObjectMapper#isEnabled(SerializationFeature)
     */
    public boolean isEnabled(SerializationFeature feature) {
        final Boolean enabled = serializationFeatures.get(feature);
        if (enabled != null) {
            return enabled;
        }
        return feature.enabledByDefault();
    }

    /**
     * Enables the given {@link SerializationFeature}s.
     *
     * @param features a set of features to enable
     * @see ObjectMapper#enable(SerializationFeature)
     */
    public void enable(SerializationFeature... features) {
        for (SerializationFeature feature : features) {
            serializationFeatures.put(feature, Boolean.TRUE);
        }
    }

    /**
     * Disables the given {@link SerializationFeature}s.
     *
     * @param features a set of features to disable
     * @see ObjectMapper#disable(SerializationFeature)
     */
    public void disable(SerializationFeature... features) {
        for (SerializationFeature feature : features) {
            serializationFeatures.put(feature, Boolean.FALSE);
        }
    }

    /**
     * Returns true if the given {@link JsonGenerator.Feature} is enabled.
     *
     * @param feature a given feature
     * @return {@code true} if {@code feature} is enabled
     * @see ObjectMapper#isEnabled(JsonGenerator.Feature)
     */
    public boolean isEnabled(JsonGenerator.Feature feature) {
        final Boolean enabled = generatorFeatures.get(feature);
        if (enabled != null) {
            return enabled;
        }
        return feature.enabledByDefault();
    }

    /**
     * Enables the given {@link JsonGenerator.Feature}s.
     *
     * @param features a set of features to enable
     * @see JsonFactory#enable(JsonGenerator.Feature)
     */
    public void enable(JsonGenerator.Feature... features) {
        for (JsonGenerator.Feature feature : features) {
            generatorFeatures.put(feature, Boolean.TRUE);
        }
    }

    /**
     * Disables the given {@link JsonGenerator.Feature}s.
     *
     * @param features a set of features to disable
     * @see JsonFactory#disable(JsonGenerator.Feature)
     */
    public void disable(JsonGenerator.Feature... features) {
        for (JsonGenerator.Feature feature : features) {
            generatorFeatures.put(feature, Boolean.FALSE);
        }
    }

    /**
     * Returns true if the given {@link JsonParser.Feature} is enabled.
     *
     * @param feature a given feature
     * @return {@code true} if {@code feature} is enabled
     * @see ObjectMapper#isEnabled(JsonParser.Feature)
     */
    public boolean isEnabled(JsonParser.Feature feature) {
        final Boolean enabled = parserFeatures.get(feature);
        if (enabled != null) {
            return enabled;
        }
        return feature.enabledByDefault();
    }

    /**
     * Enables the given {@link JsonParser.Feature}s.
     *
     * @param features a set of features to enable
     * @see JsonFactory#enable(JsonParser.Feature)
     */
    public void enable(JsonParser.Feature... features) {
        for (JsonParser.Feature feature : features) {
            parserFeatures.put(feature, Boolean.TRUE);
        }
    }

    /**
     * Disables the given {@link JsonParser.Feature}s.
     *
     * @param features a set of features to disable
     * @see JsonFactory#disable(JsonParser.Feature)
     */
    public void disable(JsonParser.Feature... features) {
        for (JsonParser.Feature feature : features) {
            parserFeatures.put(feature, Boolean.FALSE);
        }
    }

    /**
     * Returns true if the given {@link JsonFactory.Feature} is enabled.
     *
     * @param feature a given feature
     * @return {@code true} if {@code feature} is enabled
     * @see ObjectMapper#isEnabled(JsonFactory.Feature)
     */
    public boolean isEnabled(JsonFactory.Feature feature) {
        final Boolean enabled = factoryFeatures.get(feature);
        if (enabled != null) {
            return enabled;
        }
        return feature.enabledByDefault();
    }

    /**
     * Enables the given {@link JsonFactory.Feature}s.
     *
     * @param features a set of features to enable
     * @see JsonFactory#enable(JsonFactory.Feature)
     */
    public void enable(JsonFactory.Feature... features) {
        for (JsonFactory.Feature feature : features) {
            factoryFeatures.put(feature, Boolean.TRUE);
        }
    }

    /**
     * Disables the given {@link JsonFactory.Feature}s.
     *
     * @param features a set of features to disable
     * @see JsonFactory#disable(JsonFactory.Feature)
     */
    public void disable(JsonFactory.Feature... features) {
        for (JsonFactory.Feature feature : features) {
            factoryFeatures.put(feature, Boolean.FALSE);
        }
    }

    /**
     * Builds a new {@link ObjectMapper} instance with the given {@link JsonFactory} instance.
     *
     * @param factory a {@link JsonFactory}
     * @return a configured {@link ObjectMapper} instance
     */
    public ObjectMapper build(JsonFactory factory) {
        final ObjectMapper mapper = new ObjectMapper(factory);

        for (Module module : modules) {
            mapper.registerModule(module);
        }

        for (Map.Entry<MapperFeature, Boolean> entry : mapperFeatures.entrySet()) {
            mapper.configure(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<DeserializationFeature, Boolean> entry : deserializationFeatures.entrySet()) {
            mapper.configure(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<SerializationFeature, Boolean> entry : serializationFeatures.entrySet()) {
            mapper.configure(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<JsonGenerator.Feature, Boolean> entry : generatorFeatures.entrySet()) {
            mapper.getFactory().configure(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<JsonParser.Feature, Boolean> entry : parserFeatures.entrySet()) {
            mapper.getFactory().configure(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<JsonFactory.Feature, Boolean> entry : factoryFeatures.entrySet()) {
            mapper.getFactory().configure(entry.getKey(), entry.getValue());
        }

        if (annotationIntrospector != null) {
            mapper.setAnnotationIntrospector(annotationIntrospector);
        }

        if (dateFormat != null) {
            mapper.setDateFormat(dateFormat);
        }

        if (defaultTyping != null) {
            mapper.setDefaultTyping(defaultTyping);
        }

        if (filters != null) {
            mapper.setFilters(filters);
        }

        if (handlerInstantiator != null) {
            mapper.setHandlerInstantiator(handlerInstantiator);
        }

        if (injectableValues != null) {
            mapper.setInjectableValues(injectableValues);
        }

        if (locale != null) {
            mapper.setLocale(locale);
        }

        if (mixinAnnotations != null) {
            mapper.setMixInAnnotations(mixinAnnotations);
        }

        if (nodeFactory != null) {
            mapper.setNodeFactory(nodeFactory);
        }

        if (propertyNamingStrategy != null) {
            mapper.setPropertyNamingStrategy(propertyNamingStrategy);
        }

        if (serializationInclusion != null) {
            mapper.setSerializationInclusion(serializationInclusion);
        }

        if (serializerFactory != null) {
            mapper.setSerializerFactory(serializerFactory);
        }

        if (serializerProvider != null) {
            mapper.setSerializerProvider(serializerProvider);
        }

        if (subtypeResolver != null) {
            mapper.setSubtypeResolver(subtypeResolver);
        }

        if (timeZone != null) {
            mapper.setTimeZone(timeZone);
        }

        if (typeFactory != null) {
            mapper.setTypeFactory(typeFactory);
        }

        for (Map.Entry<PropertyAccessor, JsonAutoDetect.Visibility> rule : visibilityRules.entrySet()) {
            mapper.setVisibility(rule.getKey(), rule.getValue());
        }

        if (visibilityChecker != null) {
            mapper.setVisibilityChecker(visibilityChecker);
        }


        return mapper;
    }

    /**
     * Builds a new {@link ObjectMapper} instance with a default {@link JsonFactory} instance.
     *
     * @return a configured {@link ObjectMapper} instance
     */
    public ObjectMapper build() {
        return build(new JsonFactory());
    }

    /**
     * Creates a copy of {@code this}.
     *
     * @return a copy of {@code this}
     */
    public ObjectMapperFactory copy() {
        final ObjectMapperFactory factory = new ObjectMapperFactory();

        factory.modules.addAll(modules);
        factory.mapperFeatures.putAll(mapperFeatures);
        factory.deserializationFeatures.putAll(deserializationFeatures);
        factory.serializationFeatures.putAll(serializationFeatures);
        factory.generatorFeatures.putAll(generatorFeatures);
        factory.parserFeatures.putAll(parserFeatures);
        factory.factoryFeatures.putAll(factoryFeatures);
        factory.visibilityRules.putAll(visibilityRules);

        factory.annotationIntrospector = annotationIntrospector;
        factory.dateFormat = dateFormat;
        factory.defaultTyping = defaultTyping;
        factory.filters = filters;
        factory.handlerInstantiator = handlerInstantiator;
        factory.injectableValues = injectableValues;
        factory.locale = locale;
        factory.mixinAnnotations = mixinAnnotations;
        factory.nodeFactory = nodeFactory;
        factory.propertyNamingStrategy = propertyNamingStrategy;
        factory.serializationInclusion = serializationInclusion;
        factory.serializerFactory = serializerFactory;
        factory.serializerProvider = serializerProvider;
        factory.subtypeResolver = subtypeResolver;
        factory.timeZone = timeZone;
        factory.visibilityChecker = visibilityChecker;

        return factory;
    }
}
