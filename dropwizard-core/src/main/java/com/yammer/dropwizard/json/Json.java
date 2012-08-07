package com.yammer.dropwizard.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.TreeTraversingParser;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.fasterxml.jackson.datatype.guava.GuavaModule;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.ByteBuffer;

/**
 * A basic class for JSON parsing and generating.
 *
 * <p>By default, {@link Json} is configured to:</p>
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
    protected JsonFactory factory;
    protected ObjectMapper mapper;
    protected TypeFactory typeFactory;

    /**
     * Creates a new {@link Json} instance.
     */
    public Json() {
        this.factory = new MappingJsonFactory();
        factory.enable(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT);
        factory.enable(JsonGenerator.Feature.AUTO_CLOSE_TARGET);
        factory.enable(JsonGenerator.Feature.QUOTE_FIELD_NAMES);
        factory.enable(JsonParser.Feature.ALLOW_COMMENTS);
        factory.enable(JsonParser.Feature.AUTO_CLOSE_SOURCE);

        this.mapper = (ObjectMapper) factory.getCodec();
        mapper.setPropertyNamingStrategy(AnnotationSensitivePropertyNamingStrategy.INSTANCE);
        mapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        mapper.disable(SerializationFeature.WRITE_ENUMS_USING_TO_STRING);
        mapper.disable(DeserializationFeature.READ_ENUMS_USING_TO_STRING);
        mapper.registerModule(new GuavaModule());
        mapper.registerModule(new LogbackModule());
        mapper.registerModule(new GuavaExtrasModule());

        this.typeFactory = mapper.getTypeFactory();
    }

    public ObjectMapper getObjectMapper() {
        return mapper;
    }



    /**
     * Registers a module that can extend functionality provided by this class; for example, by
     * adding providers for custom serializers and deserializers.
     *
     * @param module Module to register
     * @see ObjectMapper#registerModule(com.fasterxml.jackson.databind.Module)
     */
    public void registerModule(Module module) {
        mapper.registerModule(module);
    }

    /**
     * Returns true if the given {@link DeserializationFeature} is enabled.
     *
     * @param feature    a given feature
     * @return {@code true} if {@code feature} is enabled
     * @see ObjectMapper#isEnabled(com.fasterxml.jackson.databind.DeserializationFeature)
     */
    public boolean isEnabled(DeserializationFeature feature) {
        return mapper.isEnabled(feature);
    }

    /**
     * Enables the given {@link DeserializationFeature}s.
     *
     * @param features    a set of features to enable
     * @see ObjectMapper#enable(com.fasterxml.jackson.databind.DeserializationFeature)
     */
    public void enable(DeserializationFeature... features) {
        for (DeserializationFeature feature : features) {
            mapper.enable(feature);
        }
    }

    /**
     * Disables the given {@link DeserializationFeature}s.
     *
     * @param features    a set of features to disable
     * @see ObjectMapper#disable(com.fasterxml.jackson.databind.DeserializationFeature)
     */
    public void disable(DeserializationFeature... features) {
        for (DeserializationFeature feature : features) {
            mapper.disable(feature);
        }
    }

    /**
     * Returns true if the given {@link SerializationFeature} is enabled.
     *
     * @param feature    a given feature
     * @return {@code true} if {@code feature} is enabled
     * @see ObjectMapper#isEnabled(com.fasterxml.jackson.databind.SerializationFeature)
     */
    public boolean isEnabled(SerializationFeature feature) {
        return mapper.isEnabled(feature);
    }

    /**
     * Enables the given {@link SerializationFeature}s.
     *
     * @param features    a set of features to enable
     * @see ObjectMapper#enable(com.fasterxml.jackson.databind.SerializationFeature)
     */
    public void enable(SerializationFeature... features) {
        for (SerializationFeature feature : features) {
            mapper.enable(feature);
        }
    }

    /**
     * Disables the given {@link SerializationFeature}s.
     *
     * @param features    a set of features to disable
     * @see ObjectMapper#disable(com.fasterxml.jackson.databind.SerializationFeature)
     */
    public void disable(SerializationFeature... features) {
        for (SerializationFeature feature : features) {
            mapper.disable(feature);
        }
    }

    /**
     * Returns true if the given {@link JsonGenerator.Feature} is enabled.
     *
     * @param feature    a given feature
     * @return {@code true} if {@code feature} is enabled
     * @see ObjectMapper#isEnabled(com.fasterxml.jackson.core.JsonGenerator.Feature)
     */
    public boolean isEnabled(JsonGenerator.Feature feature) {
        return mapper.isEnabled(feature);
    }

    /**
     * Enables the given {@link JsonGenerator.Feature}s.
     *
     * @param features    a set of features to enable
     * @see JsonFactory#enable(com.fasterxml.jackson.core.JsonGenerator.Feature)
     */
    public void enable(JsonGenerator.Feature... features) {
        for (JsonGenerator.Feature feature : features) {
            factory.enable(feature);
        }
    }

    /**
     * Disables the given {@link JsonGenerator.Feature}s.
     *
     * @param features    a set of features to disable
     * @see JsonFactory#disable(com.fasterxml.jackson.core.JsonGenerator.Feature)
     */
    public void disable(JsonGenerator.Feature... features) {
        for (JsonGenerator.Feature feature : features) {
            factory.disable(feature);
        }
    }

    /**
     * Returns true if the given {@link JsonParser.Feature} is enabled.
     *
     * @param feature    a given feature
     * @return {@code true} if {@code feature} is enabled
     * @see ObjectMapper#isEnabled(com.fasterxml.jackson.core.JsonParser.Feature)
     */
    public boolean isEnabled(JsonParser.Feature feature) {
        return mapper.isEnabled(feature);
    }

    /**
     * Enables the given {@link JsonParser.Feature}s.
     *
     * @param features    a set of features to enable
     * @see JsonFactory#enable(com.fasterxml.jackson.core.JsonParser.Feature)
     */
    public void enable(JsonParser.Feature... features) {
        for (JsonParser.Feature feature : features) {
            factory.enable(feature);
        }
    }

    /**
     * Disables the given {@link JsonParser.Feature}s.
     *
     * @param features    a set of features to disable
     * @see JsonFactory#disable(com.fasterxml.jackson.core.JsonParser.Feature)
     */
    public void disable(JsonParser.Feature... features) {
        for (JsonParser.Feature feature : features) {
            factory.disable(feature);
        }
    }

    /**
     * Returns {@code true} if the mapper can find a serializer for instances of given class
     * (potentially serializable), {@code false} otherwise (not serializable).
     *
     * @param type    the type of object to serialize
     * @return {@code true} if instances of {@code type} are potentially serializable
     */
    public boolean canSerialize(Class<?> type) {
        return mapper.canSerialize(type);
    }

    /**
     * Returns {@code true} if the mapper can find a deserializer for instances of given class
     * (potentially deserializable), {@code false} otherwise (not deserializable).
     *
     * @param type    the type of object to deserialize
     * @return {@code true} if instances of {@code type} are potentially deserializable
     */
    public boolean canDeserialize(Class<?> type) {
        return mapper.canDeserialize(constructType(type));
    }

    /**
     * Deserializes the given {@link File} as an instance of the given type.
     *
     * @param src          a JSON {@link File}
     * @param valueType    the {@link Class} to deserialize {@code src} as
     * @param <T>          the type of {@code valueType}
     * @return the contents of {@code src} as an instance of {@code T}
     * @throws IOException if there is an error reading from {@code src} or parsing its contents
     */
    public <T> T readValue(File src, Class<T> valueType) throws IOException {
        return mapper.readValue(src, valueType);
    }

    /**
     * Deserializes the given {@link File} as an instance of the given type.
     *
     * @param src             a JSON {@link File}
     * @param valueTypeRef    a {@link TypeReference} of the type to deserialize {@code src} as
     * @param <T>             the type of {@code valueTypeRef}
     * @return the contents of {@code src} as an instance of {@code T}
     * @throws IOException if there is an error reading from {@code src} or parsing its contents
     */
    public <T> T readValue(File src, TypeReference<T> valueTypeRef) throws IOException {
        return mapper.readValue(src, valueTypeRef);
    }

    /**
     * Deserializes the given {@link String} as an instance of the given type.
     *
     * @param content      a JSON {@link String}
     * @param valueType    the {@link Class} to deserialize {@code content} as
     * @param <T>          the type of {@code valueType}
     * @return {@code content} as an instance of {@code T}
     * @throws IOException if there is an error parsing {@code content}
     */
    public <T> T readValue(String content, Class<T> valueType) throws IOException {
        return mapper.readValue(content, valueType);
    }

    /**
     * Deserializes the given {@link String} as an instance of the given type.
     *
     * @param content         a JSON {@link String}
     * @param valueTypeRef    a {@link TypeReference} of the type to deserialize {@code content} as
     * @param <T>             the type of {@code valueTypeRef}
     * @return {@code content} as an instance of {@code T}
     * @throws IOException if there is an error parsing {@code content}
     */
    public <T> T readValue(String content, TypeReference<T> valueTypeRef) throws IOException {
        return mapper.readValue(content, valueTypeRef);
    }

    /**
     * Deserializes the given {@link Reader} as an instance of the given type.
     *
     * @param src          a JSON {@link Reader}
     * @param valueType    the {@link Class} to deserialize {@code src} as
     * @param <T>          the type of {@code valueType}
     * @return the contents of {@code src} as an instance of {@code T}
     * @throws IOException if there is an error reading from {@code src} or parsing its contents
     */
    public <T> T readValue(Reader src, Class<T> valueType) throws IOException {
        return mapper.readValue(src, valueType);
    }

    /**
     * Deserializes the given {@link Reader} as an instance of the given type.
     *
     * @param src             a JSON {@link Reader}
     * @param valueTypeRef    a {@link TypeReference} of the type to deserialize {@code src} as
     * @param <T>             the type of {@code valueTypeRef}
     * @return the contents of {@code src} as an instance of {@code T}
     * @throws IOException if there is an error reading from {@code src} or parsing its contents
     */
    public <T> T readValue(Reader src, TypeReference<T> valueTypeRef) throws IOException {
        return mapper.readValue(src, valueTypeRef);
    }

    /**
     * Deserializes the given {@link InputStream} as an instance of the given type.
     *
     * @param src          a JSON {@link InputStream}
     * @param valueType    the {@link Class} to deserialize {@code src} as
     * @param <T>          the type of {@code valueType}
     * @return the contents of {@code src} as an instance of {@code T}
     * @throws IOException if there is an error reading from {@code src} or parsing its contents
     */
    public <T> T readValue(InputStream src, Class<T> valueType) throws IOException {
        return mapper.readValue(src, valueType);
    }

    /**
     * Deserializes the given {@link InputStream} as an instance of the given type.
     *
     * @param src             a JSON {@link InputStream}
     * @param valueTypeRef    a {@link TypeReference} of the type to deserialize {@code src} as
     * @param <T>             the type of {@code valueTypeRef}
     * @return the contents of {@code src} as an instance of {@code T}
     * @throws IOException if there is an error reading from {@code src} or parsing its contents
     */
    public <T> T readValue(InputStream src, TypeReference<T> valueTypeRef) throws IOException {
        return mapper.readValue(src, valueTypeRef);
    }

    /**
     * Deserializes the given {@link InputStream} as an instance of the given type.
     *
     * @param src          a JSON {@link InputStream}
     * @param valueType    the {@link Type} to deserialize {@code src} as
     * @param <T>          the type of {@code valueType}
     * @return the contents of {@code src} as an instance of {@code T}
     * @throws IOException if there is an error reading from {@code src} or parsing its contents
     */
    public <T> T readValue(InputStream src, Type valueType) throws IOException {
        return mapper.readValue(src, constructType(valueType));
    }

    /**
     * Deserializes the given byte array as an instance of the given type.
     *
     * @param src          a JSON byte array
     * @param valueType    the {@link Class} to deserialize {@code src} as
     * @param <T>          the type of {@code valueType}
     * @return the contents of {@code src} as an instance of {@code T}
     * @throws IOException if there is an error parsing {@code src}
     */
    public <T> T readValue(byte[] src, Class<T> valueType) throws IOException {
        return mapper.readValue(src, valueType);
    }

    /**
     * Deserializes a subset of the given byte array as an instance of the given type.
     *
     * @param src          a JSON byte array
     * @param offset       the offset into {@code src} of the subset
     * @param len          the length of the subset of {@code src}
     * @param valueType    the {@link Class} to deserialize {@code src} as
     * @param <T>          the type of {@code valueType}
     * @return the contents of {@code src} as an instance of {@code T}
     * @throws IOException if there is an error parsing {@code src}
     */
    public <T> T readValue(byte[] src, int offset, int len, Class<T> valueType) throws IOException {
        return mapper.readValue(src, offset, len, valueType);
    }

    /**
     * Deserializes the given byte array as an instance of the given type.
     *
     * @param src             a JSON byte array
     * @param valueTypeRef    a {@link TypeReference} of the type to deserialize {@code src} as
     * @param <T>             the type of {@code valueTypeRef}
     * @return the contents of {@code src} as an instance of {@code T}
     * @throws IOException if there is an error parsing {@code src}
     */
    public <T> T readValue(byte[] src, TypeReference<T> valueTypeRef) throws IOException {
        return mapper.readValue(src, valueTypeRef);
    }

    /**
     * Deserializes a subset of the given byte array as an instance of the given type.
     *
     * @param src             a JSON byte array
     * @param offset       the offset into {@code src} of the subset
     * @param len          the length of the subset of {@code src}
     * @param valueTypeRef    a {@link TypeReference} of the type to deserialize {@code src} as
     * @param <T>             the type of {@code valueTypeRef}
     * @return the contents of {@code src} as an instance of {@code T}
     * @throws IOException if there is an error parsing {@code src}
     */
    public <T> T readValue(byte[] src, int offset, int len, TypeReference<T> valueTypeRef) throws IOException {
        return mapper.readValue(src, offset, len, valueTypeRef);
    }

    /**
     * Deserializes the given {@link JsonNode} as an instance of the given type.
     *
     * @param root         a {@link JsonNode}
     * @param valueType    the {@link Class} to deserialize {@code src} as
     * @param <T>          the type of {@code valueType}
     * @return the contents of {@code src} as an instance of {@code T}
     * @throws IOException if there is an error mapping {@code src} to {@code T}
     */
    public <T> T readValue(JsonNode root, Class<T> valueType) throws IOException {
        return mapper.readValue(new TreeTraversingParser(root), valueType);
    }

    /**
     * Deserializes the given {@link JsonNode} as an instance of the given type.
     *
     * @param root            a {@link JsonNode}
     * @param valueTypeRef    a {@link TypeReference} of the type to deserialize {@code src} as
     * @param <T>             the type of {@code valueTypeRef}
     * @return the contents of {@code src} as an instance of {@code T}
     * @throws IOException if there is an error mapping {@code src} to {@code T}
     */
    public <T> T readValue(JsonNode root, TypeReference<T> valueTypeRef) throws IOException {
        return mapper.readValue(new TreeTraversingParser(root), valueTypeRef);
    }

    /**
     * Deserializes the given {@link ByteBuffer} as an instance of the given type.
     *
     * @param src a {@link ByteBuffer}
     * @param valueType the {@link Class} to deserialize {@code src} as
     * @param <T>       the type of {@code valueType}
     * @return the contents of {@code src} as an instance of {@code T}
     * @throws IOException if there is an error mapping {@code src} to {@code T}
     */
    public <T> T readValue(ByteBuffer src, Class<T> valueType) throws IOException {
        final int bufLength = src.limit() - src.position();

        if (src.hasArray()) {
            return readValue(src.array(), src.arrayOffset(), bufLength, valueType);
        } else {
            final byte[] bytes = new byte[bufLength];
            src.get(bytes);
            return readValue(bytes, 0, bufLength, valueType);
        }
    }

    /**
     * Deserializes the given {@link ByteBuffer} as an instance of the given type.
     *
     * @param src a ByteBuffer containing some JSON
     * @param valueTypeRef a {@link TypeReference} of the type to deserialize {@code src} as
     * @param <T>          the type of {@code valueTypeRef}
     * @return the contents of {@code src} as an instance of {@code T}
     * @throws IOException if there is an error parsing {@code src}
     */
    public <T> T readValue(ByteBuffer src, TypeReference<T> valueTypeRef) throws IOException {
        final int bufLength = src.limit() - src.position();

        if (src.hasArray()) {
            return readValue(src.array(), src.arrayOffset(), bufLength, valueTypeRef);
        } else {
            final byte[] bytes = new byte[bufLength];
            src.get(bytes);
            return readValue(bytes, 0, bufLength, valueTypeRef);
        }
    }

    /**
     * Serializes the given object to the given {@link File}.
     *
     * @param output the {@link File} to which the JSON will be written
     * @param value  the object to serialize into {@code output}
     * @throws IOException if there is an error writing to {@code output} or serializing {@code
     *                     value}
     */
    public void writeValue(File output, Object value) throws IOException {
        mapper.writeValue(output, value);
    }

    /**
     * Serializes the given object to the given {@link OutputStream}.
     *
     * @param output the {@link OutputStream} to which the JSON will be written
     * @param value  the object to serialize into {@code output}
     * @throws IOException if there is an error writing to {@code output} or serializing {@code
     *                     value}
     */
    public void writeValue(OutputStream output, Object value) throws IOException {
        mapper.writeValue(output, value);
    }

    /**
     * Serializes the given object to the given {@link Writer}.
     *
     * @param output the {@link Writer} to which the JSON will be written
     * @param value  the object to serialize into {@code output}
     * @throws IOException if there is an error writing to {@code output} or serializing {@code
     *                     value}
     */
    public void writeValue(Writer output, Object value) throws IOException {
        mapper.writeValue(output, value);
    }

    /**
     * Returns the given object as a JSON string.
     *
     * @param value    an object
     * @return {@code value} as a JSON string
     * @throws IllegalArgumentException if there is an error encoding {@code value}
     */
    public String writeValueAsString(Object value) throws IllegalArgumentException {
        try {
            return mapper.writeValueAsString(value);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Returns the given object as a JSON byte array.
     *
     * @param value    an object
     * @return {@code value} as a JSON byte array
     * @throws IllegalArgumentException if there is an error encoding {@code value}
     */
    public byte[] writeValueAsBytes(Object value) throws IllegalArgumentException {
        try {
            return mapper.writeValueAsBytes(value);
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    /**
     * Returns the given object as a JSON {@link ByteBuffer}.
     *
     * @param value an object
     * @return {@code value} as a JSON {@link ByteBuffer}
     * @throws IllegalArgumentException if there is an error encoding {@code value}
     */
    public ByteBuffer writeValueAsByteBuffer(Object value) throws IllegalArgumentException {
        return ByteBuffer.wrap(writeValueAsBytes(value));
    }

    /**
     * Returns the given object as a {@link JsonNode}.
     *
     * @param value    an object
     * @return {@code value} as a {@link JsonNode}
     * @throws IllegalArgumentException if there is an error encoding {@code value}
     */
    public JsonNode writeValueAsTree(Object value) throws IllegalArgumentException {
        return mapper.valueToTree(value);
    }

    /**
     * Deserializes the given YAML {@link File} as an instance of the given type.
     * <p><b>N.B.:</b> All tags, comments, and non-JSON elements of the YAML file will be elided.</p>
     *
     * @param src          a YAML {@link File}
     * @param valueType    the {@link Class} to deserialize {@code src} as
     * @param <T>          the type of {@code valueType}
     * @return the contents of {@code src} as an instance of {@code T}
     * @throws IOException if there is an error reading from {@code src} or parsing its contents
     */
    public <T> T readYamlValue(File src, Class<T> valueType) throws IOException {
        final YamlConverter converter = new YamlConverter(this, factory);
        return readValue(converter.convert(src), valueType);
    }

    /**
     * Deserializes the given YAML {@link File} as an instance of the given type.
     * <p><b>N.B.:</b> All tags, comments, and non-JSON elements of the YAML file will be elided.</p>
     *
     * @param src             a YAML {@link File}
     * @param valueTypeRef    a {@link TypeReference} of the type to deserialize {@code src} as
     * @param <T>             the type of {@code valueTypeRef}
     * @return the contents of {@code src} as an instance of {@code T}
     * @throws IOException if there is an error reading from {@code src} or parsing its contents
     */
    public <T> T readYamlValue(File src, TypeReference<T> valueTypeRef) throws IOException {
        final YamlConverter converter = new YamlConverter(this, factory);
        return readValue(converter.convert(src), valueTypeRef);
    }

    private JavaType constructType(Type type) {
        return typeFactory.constructType(type);
    }
}
