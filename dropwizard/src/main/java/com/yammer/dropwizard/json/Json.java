package com.yammer.dropwizard.json;

import com.fasterxml.jackson.module.guava.GuavaModule;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.map.*;
import org.codehaus.jackson.map.type.TypeFactory;
import org.codehaus.jackson.type.JavaType;
import org.codehaus.jackson.type.TypeReference;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

// TODO: 10/12/11 <coda> -- write tests for JSON
// TODO: 10/12/11 <coda> -- write docs for JSON

/**
 * It's configured to:
 * <ul>
 *     <li>Automatically close JSON content, if possible.</li>
 *     <li>Automatically close input and output streams.</li>
 *     <li>Quote field names.</li>
 *     <li>Allow both C-style line and block comments.</li>
 *     <li>Not fail when encountering unknown properties.</li>
 *     <li>Read and write enums using {@code toString()}.</li>
 *     <li>Use {@code snake_case} for property names when encoding and decoding
 *         classes annotated with {@link JsonSnakeCase}.</li>
 * </ul>
 */
public class Json {
    private Json() {
        // singleton
    }

    private static final JsonFactory factory;
    private static final ObjectMapper mapper;
    private static final TypeFactory typeFactory;

    static {
        factory = new MappingJsonFactory();
        factory.enable(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT);
        factory.enable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        factory.enable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        factory.enable(JsonParser.Feature.ALLOW_COMMENTS);
        factory.enable(JsonParser.Feature.AUTO_CLOSE_SOURCE);

        mapper = (ObjectMapper) factory.getCodec();
        mapper.setPropertyNamingStrategy(AnnotationSensitivePropertyNamingStrategy.INSTANCE);
        mapper.disable(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.disable(SerializationConfig.Feature.WRITE_ENUMS_USING_TO_STRING);
        mapper.disable(DeserializationConfig.Feature.READ_ENUMS_USING_TO_STRING);
        mapper.registerModule(new GuavaModule());

        typeFactory = mapper.getTypeFactory();
    }

    private static JavaType constructType(Type type) {
        return typeFactory.constructType(type);
    }

    public static void registerModule(Module module) {
        mapper.registerModule(module);
    }

    public static void configure(JsonGenerator.Feature feature, boolean enabled) {
        mapper.configure(feature, enabled);
    }

    public static void configure(JsonParser.Feature feature, boolean enabled) {
        mapper.configure(feature, enabled);
    }

    public static void configure(SerializationConfig.Feature feature, boolean enabled) {
        mapper.configure(feature, enabled);
    }

    public static void configure(DeserializationConfig.Feature feature, boolean enabled) {
        mapper.configure(feature, enabled);
    }

    public static boolean canSerialize(Class<?> type) {
        return mapper.canSerialize(type);
    }

    public static boolean canDeserialize(Class<?> type) {
        return mapper.canDeserialize(constructType(type));
    }

    public static void write(OutputStream output, Object o) throws IOException {
        mapper.writeValue(output, o);
    }

    public static void write(File file, Object o) throws IOException {
        mapper.writeValue(file, o);
    }

    public static String write(Object o) throws IOException {
        return mapper.writeValueAsString(o);
    }

    public static <T> T read(InputStream input, Class<T> type) throws IOException {
        return mapper.readValue(input, constructType(type));
    }

    public static <T> T read(InputStream input, Type type) throws IOException {
        return mapper.readValue(input, constructType(type));
    }

    public static <T> T read(InputStream input, TypeReference<T> ref) throws IOException {
        return mapper.readValue(input, ref);
    }

    public static <T> T read(JsonNode json, Class<T> klass) throws IOException {
        return mapper.readValue(json, constructType(klass));
    }

    public static <T> T read(JsonNode json, TypeReference<T> ref) throws IOException {
        return mapper.readValue(json, ref);
    }

    public static <T> T read(File file, Class<T> klass) throws IOException {
        return mapper.readValue(file, constructType(klass));
    }

    public static <T> T read(File file, TypeReference<T> ref) throws IOException {
        return mapper.readValue(file, ref);
    }

    public static <T> T read(String json, Class<T> klass) throws IOException {
        return mapper.readValue(json, constructType(klass));
    }

    public static <T> T read(String json, TypeReference<T> ref) throws IOException {
        return mapper.readValue(json, ref);
    }
    
}
