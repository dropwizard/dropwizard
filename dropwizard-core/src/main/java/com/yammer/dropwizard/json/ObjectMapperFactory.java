package com.yammer.dropwizard.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

public class ObjectMapperFactory {
    private final List<Module> modules;
    private final Map<MapperFeature, Boolean> mapperFeatures;
    private final Map<DeserializationFeature, Boolean> deserializationFeatures;
    private final Map<SerializationFeature, Boolean> serializationFeatures;
    private final Map<JsonGenerator.Feature, Boolean> generatorFeatures;
    private final Map<JsonParser.Feature, Boolean> parserFeatures;
    private final Map<JsonFactory.Feature, Boolean> factoryFeatures;

    private ObjectMapperFactory() {
        this.modules = Lists.newArrayList();
        this.mapperFeatures = Maps.newHashMap();
        this.deserializationFeatures = Maps.newHashMap();
        this.serializationFeatures = Maps.newHashMap();
        this.generatorFeatures = Maps.newHashMap();
        this.parserFeatures = Maps.newHashMap();
        this.factoryFeatures = Maps.newHashMap();
    }

    public static ObjectMapperFactory defaultInstance() {
        final ObjectMapperFactory factory = new ObjectMapperFactory();

        factory.enable(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT);
        factory.enable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        factory.enable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        factory.enable(JsonParser.Feature.ALLOW_COMMENTS);
        factory.enable(JsonParser.Feature.AUTO_CLOSE_SOURCE);

        factory.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        factory.disable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        factory.disable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);

        factory.registerModule(new GuavaModule());
        factory.registerModule(new LogbackModule());
        factory.registerModule(new GuavaExtrasModule());

        return factory;
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
     * Returns true if the given {@link MapperFeature} is
     * enabled.
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
     * Returns true if the given {@link DeserializationFeature} is
     * enabled.
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
     * Returns true if the given {@link SerializationFeature} is
     * enabled.
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
     * Returns true if the given {@link JsonGenerator.Feature} is
     * enabled.
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
            mapper.getJsonFactory().configure(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<JsonParser.Feature, Boolean> entry : parserFeatures.entrySet()) {
            mapper.getJsonFactory().configure(entry.getKey(), entry.getValue());
        }

        for (Map.Entry<JsonFactory.Feature, Boolean> entry : factoryFeatures.entrySet()) {
            mapper.getJsonFactory().configure(entry.getKey(), entry.getValue());
        }

        mapper.setPropertyNamingStrategy(AnnotationSensitivePropertyNamingStrategy.INSTANCE);

        return mapper;
    }

    public ObjectMapper build() {
        return build(new JsonFactory());
    }

    @Override
    @SuppressWarnings("CloneDoesntCallSuperClone")
    public ObjectMapperFactory clone() throws CloneNotSupportedException {
        final ObjectMapperFactory factory = new ObjectMapperFactory();
        factory.modules.addAll(modules);
        factory.mapperFeatures.putAll(mapperFeatures);
        factory.deserializationFeatures.putAll(deserializationFeatures);
        factory.serializationFeatures.putAll(serializationFeatures);
        factory.generatorFeatures.putAll(generatorFeatures);
        factory.parserFeatures.putAll(parserFeatures);
        factory.factoryFeatures.putAll(factoryFeatures);
        return factory;
    }
}
