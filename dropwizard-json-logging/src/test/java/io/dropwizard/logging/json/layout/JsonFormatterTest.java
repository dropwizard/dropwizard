package io.dropwizard.logging.json.layout;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.util.Maps;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.SortedMap;
import java.util.TreeMap;

import static org.assertj.core.api.Assertions.assertThat;

class JsonFormatterTest {

    private final SortedMap<String, Object> map = new TreeMap<>(Maps.of(
            "name", "Jim",
            "hobbies", Arrays.asList("Reading", "Biking", "Snorkeling")));
    private final ObjectMapper objectMapper = Jackson.newObjectMapper();

    @Test
    void testNoPrettyPrintNoLineSeparator() throws IOException {
        JsonFormatter formatter = new JsonFormatter(objectMapper, false, false);

        final JsonNode actual = objectMapper.readTree(formatter.toJson(map));
        final JsonNode expected = objectMapper.readTree("{\"name\":\"Jim\",\"hobbies\":[\"Reading\",\"Biking\",\"Snorkeling\"]}");
        assertThat(actual).isEqualTo(expected);
    }


    @Test
    void testNoPrettyPrintWithLineSeparator() throws IOException {
        JsonFormatter formatter = new JsonFormatter(objectMapper, false, true);

        final String content = formatter.toJson(map);
        assertThat(content).endsWith(System.lineSeparator());
        final JsonNode actual = objectMapper.readTree(content);
        final JsonNode expected = objectMapper.readTree("{\"name\":\"Jim\",\"hobbies\":[\"Reading\",\"Biking\",\"Snorkeling\"]}");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    void testPrettyPrintWithLineSeparator() {
        JsonFormatter formatter = new JsonFormatter(objectMapper, true, true);
        assertThat(formatter.toJson(map)).isEqualToNormalizingNewlines(
                "{\n" +
                "  \"hobbies\" : [ \"Reading\", \"Biking\", \"Snorkeling\" ],\n" +
                "  \"name\" : \"Jim\"\n" +
                "}\n");
    }

    @Test
    void testPrettyPrintNoLineSeparator() {
        JsonFormatter formatter = new JsonFormatter(objectMapper, true, false);
        assertThat(formatter.toJson(map)).isEqualToNormalizingNewlines(
                "{\n" +
                "  \"hobbies\" : [ \"Reading\", \"Biking\", \"Snorkeling\" ],\n" +
                "  \"name\" : \"Jim\"\n" +
                "}");
    }
}
