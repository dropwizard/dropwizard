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

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class JsonTest {
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
        assertThat(json.canSerialize(String.class),
                   is(true));
    }

    @Test
    public void itCanDeserializeSupportedClasses() throws Exception {
        assertThat(json.canDeserialize(String.class),
                   is(true));
    }

    @Test
    public void enablesAndDisablesJsonGeneratorFeatures() throws Exception {
        assertThat(json.isEnabled(JsonGenerator.Feature.ESCAPE_NON_ASCII),
                   is(false));

        json.enable(JsonGenerator.Feature.ESCAPE_NON_ASCII);
        
        assertThat(json.isEnabled(JsonGenerator.Feature.ESCAPE_NON_ASCII),
                   is(true));

        json.disable(JsonGenerator.Feature.ESCAPE_NON_ASCII);

        assertThat(json.isEnabled(JsonGenerator.Feature.ESCAPE_NON_ASCII),
                   is(false));
    }

    @Test
    public void enablesAndDisablesJsonParserFeatures() throws Exception {
        assertThat(json.isEnabled(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER),
                   is(false));

        json.enable(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER);

        assertThat(json.isEnabled(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER),
                   is(true));

        json.disable(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER);

        assertThat(json.isEnabled(JsonParser.Feature.ALLOW_BACKSLASH_ESCAPING_ANY_CHARACTER),
                   is(false));
    }

    @Test
    public void enablesAndDisablesJsonSerializationFeatures() throws Exception {
        assertThat(json.isEnabled(SerializationFeature.INDENT_OUTPUT),
                   is(false));

        json.enable(SerializationFeature.INDENT_OUTPUT);

        assertThat(json.isEnabled(SerializationFeature.INDENT_OUTPUT),
                   is(true));

        json.disable(SerializationFeature.INDENT_OUTPUT);

        assertThat(json.isEnabled(SerializationFeature.INDENT_OUTPUT),
                   is(false));
    }

    @Test
    public void enablesAndDisablesJsonDeserializationFeatures() throws Exception {
        assertThat(json.isEnabled(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY),
                   is(false));

        json.enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

        assertThat(json.isEnabled(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY),
                   is(true));

        json.disable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY);

        assertThat(json.isEnabled(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY),
                   is(false));
    }

    @Test
    public void closesJsonContentByDefault() throws Exception {
        assertThat(json.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_JSON_CONTENT),
                   is(true));
    }

    @Test
    public void closesTargetsByDefault() throws Exception {
        assertThat(json.isEnabled(JsonGenerator.Feature.AUTO_CLOSE_TARGET),
                   is(true));
    }

    @Test
    public void quotesFieldNamesByDefault() throws Exception {
        assertThat(json.isEnabled(JsonGenerator.Feature.QUOTE_FIELD_NAMES),
                   is(true));
    }

    @Test
    public void allowsCommentsByDefault() throws Exception {
        assertThat(json.isEnabled(JsonParser.Feature.ALLOW_COMMENTS),
                   is(true));
    }

    @Test
    public void closesSourcesByDefault() throws Exception {
        assertThat(json.isEnabled(JsonParser.Feature.AUTO_CLOSE_SOURCE),
                   is(true));
    }

    @Test
    public void readsValuesFromFiles() throws Exception {
        assertThat(json.readValue(new File(Resources.getResource("json/string.json").getFile()),
                                  String.class),
                   is("a string"));

        assertThat(json.readValue(new File(Resources.getResource("json/string.json").getFile()),
                                  new TypeReference<String>() {}),
                   is("a string"));
    }

    @Test
    public void readsValuesFromStrings() throws Exception {
        assertThat(json.readValue(Resources.toString(Resources.getResource("json/string.json"),
                                                     Charsets.UTF_8),
                                  String.class),
                   is("a string"));

        assertThat(json.readValue(Resources.toString(Resources.getResource("json/string.json"),
                                                     Charsets.UTF_8),
                                  new TypeReference<String>() {}),
                   is("a string"));
    }

    @Test
    public void readsValuesFromReaders() throws Exception {
        assertThat(json.readValue(new InputStreamReader(new FileInputStream(Resources.getResource("json/string.json").getFile()), Charsets.UTF_8),
                                  String.class),
                   is("a string"));

        assertThat(json.readValue(new InputStreamReader(new FileInputStream(Resources.getResource("json/string.json").getFile()), Charsets.UTF_8),
                                  new TypeReference<String>() {}),
                   is("a string"));
    }

    @Test
    public void readsValuesFromInputStreams() throws Exception {
        assertThat(json.readValue(new FileInputStream(Resources.getResource("json/string.json").getFile()),
                                  String.class),
                   is("a string"));

        assertThat(json.readValue(new FileInputStream(Resources.getResource("json/string.json").getFile()),
                                  new TypeReference<String>() {}),
                   is("a string"));
    }

    @Test
    public void readsValuesFromByteArrays() throws Exception {
        assertThat(json.readValue(Resources.toByteArray(Resources.getResource("json/string.json")),
                                  String.class),
                   is("a string"));

        assertThat(json.readValue(Resources.toByteArray(Resources.getResource("json/string.json")),
                                  0, 10, String.class),
                   is("a string"));

        assertThat(json.readValue(Resources.toByteArray(Resources.getResource("json/string.json")),
                                  new TypeReference<String>() {}),
                   is("a string"));

        assertThat(json.readValue(Resources.toByteArray(Resources.getResource("json/string.json")),
                                  0, 10, new TypeReference<String>() {}),
                   is("a string"));
    }

    @Test
    public void readsValuesFromByteBuffers() throws Exception {
        final byte[] data = Resources.toByteArray(Resources.getResource("json/string.json"));

        // "heap" ByteBuffers
        assertThat(json.readValue(ByteBuffer.wrap(data), String.class),
                   is("a string"));
        assertThat(json.readValue(ByteBuffer.wrap(data), new TypeReference<String>() {}),
                   is("a string"));

        // "direct" ByteBuffers
        final ByteBuffer direct = ByteBuffer.allocateDirect(data.length);
        direct.put(data).flip();
        assertThat(direct.position(), is(0));
        assertThat(direct.limit(), is(data.length));
        assertThat(direct.hasArray(), is(false));
        assertThat(json.readValue(direct.duplicate(), String.class), is("a string"));
        assertThat(json.readValue(direct.duplicate(), new TypeReference<String>() {}), is("a string"));
    }

    @Test
    public void readsValuesFromJsonNodes() throws Exception {
        final JsonNode node = json.readValue(Resources.toByteArray(Resources.getResource(
                "json/string.json")), JsonNode.class);
        
        assertThat(json.readValue(node, String.class),
                   is("a string"));

        assertThat(json.readValue(node, new TypeReference<String>() {}),
                   is("a string"));
    }

    @Test
    public void serializesObjectsToJsonNodes() throws Exception {
        assertThat(json.writeValueAsTree("a string"),
                   is((JsonNode) TextNode.valueOf("a string")));
    }

    @Test
    public void serializesObjectsToStrings() throws Exception {
        assertThat(json.writeValueAsString("a string"),
                   is("\"a string\""));
    }

    @Test
    public void serializesObjectsToByteArrays() throws Exception {
        assertThat(json.writeValueAsBytes("a string"),
                   is("\"a string\"".getBytes(Charsets.UTF_8)));
    }

    @Test
    public void serializesObjectsToByteBuffers() throws Exception {
        assertThat(json.writeValueAsByteBuffer("a string"),
                   is(ByteBuffer.wrap("\"a string\"".getBytes(Charsets.UTF_8))));
    }

    @Test
    public void serializesObjectsToOutputStreams() throws Exception {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();

        json.writeValue(output, "a string");

        assertThat(output.toString(),
                   is("\"a string\""));
    }

    @Test
    public void serializesObjectsToWriters() throws Exception {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final OutputStreamWriter writer = new OutputStreamWriter(output);

        json.writeValue(writer, "a string");

        assertThat(output.toString(),
                   is("\"a string\""));
    }

    @Test
    public void serializesObjectsToFiles() throws Exception {
        final File tmp = File.createTempFile("json-test", "json");
        try {
            json.writeValue(tmp, "a string");

            assertThat(Files.toString(tmp, Charsets.UTF_8),
                       is("\"a string\""));
        } finally {
            tmp.deleteOnExit();
        }
    }

    @Test
    public void deserializesSnakeCaseFieldNames() throws Exception {
        final SnakeCaseExample example = json.readValue("{\"first_name\":\"Coda\"}",
                                                        SnakeCaseExample.class);

        assertThat(example.getFirstName(),
                   is("Coda"));
    }

    @Test
    public void serializesSnakeCaseFieldNames() throws Exception {
        final SnakeCaseExample example = new SnakeCaseExample();
        example.setFirstName("Coda");

        assertThat(json.writeValueAsString(example),
                   is("{\"first_name\":\"Coda\"}"));
    }
}
