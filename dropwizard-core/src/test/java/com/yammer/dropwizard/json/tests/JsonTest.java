package com.yammer.dropwizard.json.tests;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import com.yammer.dropwizard.json.Json;
import com.yammer.dropwizard.json.JsonSnakeCase;
import org.junit.Test;

import java.io.*;
import java.nio.ByteBuffer;

import static org.fest.assertions.api.Assertions.assertThat;

public class JsonTest {
    private static final TypeReference<String> STRING_TYPE_REF = new TypeReference<String>() {};

    @JsonSnakeCase
    private static class SnakeCaseExample {
        @JsonProperty
        private String firstName;

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }
    }

    private final Json json = new Json();

    @Test
    public void itCanSerializeSupportedClasses() throws Exception {
        assertThat(json.canSerialize(String.class))
                .isTrue();
    }

    @Test
    public void itCanDeserializeSupportedClasses() throws Exception {
        assertThat(json.canDeserialize(String.class))
                .isTrue();
    }

    @Test
    public void enablesAndDisablesJsonGeneratorFeatures() throws Exception {
        assertThat(json.isEnabled(JsonGenerator.Feature.ESCAPE_NON_ASCII))
                .isFalse();

        json.enable(JsonGenerator.Feature.ESCAPE_NON_ASCII);

        assertThat(json.isEnabled(JsonGenerator.Feature.ESCAPE_NON_ASCII))
                .isTrue();

        json.disable(JsonGenerator.Feature.ESCAPE_NON_ASCII);

        assertThat(json.isEnabled(JsonGenerator.Feature.ESCAPE_NON_ASCII))
                .isFalse();
    }

    @Test
    public void enablesAndDisablesJsonParserFeatures() throws Exception {
        assertThat(json.isEnabled(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER))
                .isFalse();

        json.enable(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER);

        assertThat(json.isEnabled(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER))
                .isTrue();

        json.disable(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER);

        assertThat(json.isEnabled(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER))
                .isFalse();
    }

    @Test
    public void enablesAndDisablesJsonSerializationFeatures() throws Exception {
        assertThat(json.isEnabled(SerializationFeature.INDENT_OUTPUT))
                .isFalse();

        json.enable(SerializationFeature.INDENT_OUTPUT);

        assertThat(json.isEnabled(SerializationFeature.INDENT_OUTPUT))
                .isTrue();

        json.disable(SerializationFeature.INDENT_OUTPUT);

        assertThat(json.isEnabled(SerializationFeature.INDENT_OUTPUT))
                .isFalse();
    }

    @Test
    public void enablesAndDisablesJsonDeserializationFeatures() throws Exception {
        assertThat(json.isEnabled(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY))
                .isFalse();

        json.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

        assertThat(json.isEnabled(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY))
                .isTrue();

        json.disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

        assertThat(json.isEnabled(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY))
                .isFalse();
    }

    @Test
    public void closesJsonContentByDefault() throws Exception {
        assertThat(json.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT))
                .isTrue();
    }

    @Test
    public void closesTargetsByDefault() throws Exception {
        assertThat(json.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_TARGET))
                .isTrue();
    }

    @Test
    public void quotesFieldNamesByDefault() throws Exception {
        assertThat(json.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES))
                .isTrue();
    }

    @Test
    public void allowsCommentsByDefault() throws Exception {
        assertThat(json.isEnabled(JsonParser.Feature.ALLOW_COMMENTS))
                .isTrue();
    }

    @Test
    public void closesSourcesByDefault() throws Exception {
        assertThat(json.isEnabled(JsonParser.Feature.AUTO_CLOSE_SOURCE))
                .isTrue();
    }

    @Test
    public void readsValuesFromFiles() throws Exception {
        assertThat(json.readValue(new File(Resources.getResource("json/string.json").getFile()),
                                  String.class))
                .isEqualTo("a string");

        assertThat(json.readValue(new File(Resources.getResource("json/string.json").getFile()),
                                  STRING_TYPE_REF))
                .isEqualTo("a string");
    }

    @Test
    public void readsValuesFromStrings() throws Exception {
        assertThat(json.readValue(Resources.toString(Resources.getResource("json/string.json"),
                                                     Charsets.UTF_8),
                                  String.class))
                .isEqualTo("a string");

        assertThat(json.readValue(Resources.toString(Resources.getResource("json/string.json"),
                                                     Charsets.UTF_8),
                                  STRING_TYPE_REF))
                .isEqualTo("a string");
    }

    @Test
    public void readsValuesFromReaders() throws Exception {
        assertThat(json.readValue(new InputStreamReader(new FileInputStream(Resources.getResource(
                "json/string.json").getFile()), Charsets.UTF_8),
                                  String.class))
                .isEqualTo("a string");

        assertThat(json.readValue(new InputStreamReader(new FileInputStream(Resources.getResource(
                "json/string.json").getFile()), Charsets.UTF_8),
                                  STRING_TYPE_REF))
                .isEqualTo("a string");
    }

    @Test
    public void readsValuesFromInputStreams() throws Exception {
        assertThat(json.readValue(new FileInputStream(Resources.getResource("json/string.json")
                                                               .getFile()),
                                  String.class))
                .isEqualTo("a string");

        assertThat(json.readValue(new FileInputStream(Resources.getResource("json/string.json")
                                                               .getFile()),
                                  STRING_TYPE_REF))
                .isEqualTo("a string");
    }

    @Test
    public void readsValuesFromByteArrays() throws Exception {
        assertThat(json.readValue(Resources.toByteArray(Resources.getResource("json/string.json")),
                                  String.class))
                .isEqualTo("a string");

        assertThat(json.readValue(Resources.toByteArray(Resources.getResource("json/string.json")),
                                  0, 10, String.class))
                .isEqualTo("a string");

        assertThat(json.readValue(Resources.toByteArray(Resources.getResource("json/string.json")),
                                  STRING_TYPE_REF))
                .isEqualTo("a string");

        assertThat(json.readValue(Resources.toByteArray(Resources.getResource("json/string.json")),
                                  0, 10, STRING_TYPE_REF))
                .isEqualTo("a string");
    }

    @Test
    public void readsValuesFromByteBuffers() throws Exception {
        final byte[] data = Resources.toByteArray(Resources.getResource("json/string.json"));

        // "heap" ByteBuffers
        assertThat(json.readValue(ByteBuffer.wrap(data), String.class))
                .isEqualTo("a string");
        assertThat(json.readValue(ByteBuffer.wrap(data), STRING_TYPE_REF))
                .isEqualTo("a string");

        // "direct" ByteBuffers
        final ByteBuffer direct = ByteBuffer.allocateDirect(data.length);
        direct.put(data).flip();
        assertThat(direct.position())
                .isZero();
        assertThat(direct.limit())
                .isEqualTo(data.length);
        assertThat(direct.hasArray())
                .isFalse();
        assertThat(json.readValue(direct.duplicate(), String.class))
                .isEqualTo("a string");
        assertThat(json.readValue(direct.duplicate(), STRING_TYPE_REF))
                .isEqualTo("a string");
    }

    @Test
    public void readsValuesFromJsonNodes() throws Exception {
        final JsonNode node = json.readValue(Resources.toByteArray(Resources.getResource(
                "json/string.json")), JsonNode.class);

        assertThat(json.readValue(node, String.class))
                .isEqualTo("a string");

        assertThat(json.readValue(node, STRING_TYPE_REF))
                .isEqualTo("a string");
    }

    @Test
    public void serializesObjectsToJsonNodes() throws Exception {
        assertThat(json.writeValueAsTree("a string"))
                .isEqualTo(TextNode.valueOf("a string"));
    }

    @Test
    public void serializesObjectsToStrings() throws Exception {
        assertThat(json.writeValueAsString("a string"))
                .isEqualTo("\"a string\"");
    }

    @Test
    public void serializesObjectsToByteArrays() throws Exception {
        assertThat(json.writeValueAsBytes("a string"))
                .isEqualTo("\"a string\"".getBytes(Charsets.UTF_8));
    }

    @Test
    public void serializesObjectsToByteBuffers() throws Exception {
        assertThat(json.writeValueAsByteBuffer("a string"))
                .isEqualTo(ByteBuffer.wrap("\"a string\"".getBytes(Charsets.UTF_8)));
    }

    @Test
    public void serializesObjectsToOutputStreams() throws Exception {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();

        json.writeValue(output, "a string");

        assertThat(output.toString())
                .isEqualTo("\"a string\"");
    }

    @Test
    public void serializesObjectsToWriters() throws Exception {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final OutputStreamWriter writer = new OutputStreamWriter(output);

        json.writeValue(writer, "a string");

        assertThat(output.toString())
                .isEqualTo("\"a string\"");
    }

    @Test
    public void serializesObjectsToFiles() throws Exception {
        final File tmp = File.createTempFile("json-test", "json");
        try {
            json.writeValue(tmp, "a string");

            assertThat(Files.toString(tmp, Charsets.UTF_8))
                    .isEqualTo("\"a string\"");
        } finally {
            tmp.deleteOnExit();
        }
    }

    @Test
    public void deserializesSnakeCaseFieldNames() throws Exception {
        final SnakeCaseExample example = json.readValue("{\"first_name\":\"Coda\"}",
                                                        SnakeCaseExample.class);

        assertThat(example.getFirstName())
                .isEqualTo("Coda");
    }

    @Test
    public void serializesSnakeCaseFieldNames() throws Exception {
        final SnakeCaseExample example = new SnakeCaseExample();
        example.setFirstName("Coda");

        assertThat(json.writeValueAsString(example))
                .isEqualTo("{\"first_name\":\"Coda\"}");
    }
}
