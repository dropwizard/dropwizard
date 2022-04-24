package io.dropwizard.logging.json.layout;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.util.Maps;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.Arrays;
import java.util.SortedMap;
import java.util.TreeMap;

import static io.dropwizard.jackson.Jackson.newObjectMapper;
import static org.assertj.core.api.Assertions.assertThat;

class JsonFormatterTest {

    private final SortedMap<String, Object> map = new TreeMap<>(Maps.of(
            "name", "Jim",
            "hobbies", Arrays.asList("Reading", "Biking", "Snorkeling")));
    private final ObjectMapper objectMapper = newObjectMapper();

    @ParameterizedTest
    @ValueSource(booleans={true, false})
    void testNoPrettyPrint(boolean appendLineSeparator) throws IOException {
        JsonFormatter formatter = new JsonFormatter(objectMapper, false, appendLineSeparator);

        final String content = formatter.toJson(map);
        if (appendLineSeparator) {
            assertThat(content).endsWith(System.lineSeparator());
        } else {
            assertThat(content).doesNotEndWith(System.lineSeparator());
        }

        assertThat(objectMapper.readTree(content))
            .isEqualTo(objectMapper.readTree("{\"name\":\"Jim\",\"hobbies\":[\"Reading\",\"Biking\",\"Snorkeling\"]}"));
    }

    @ParameterizedTest
    @ValueSource(booleans={true, false})
    void testPrettyPrint(boolean appendLineSeparator) {
        JsonFormatter formatter = new JsonFormatter(objectMapper, true, appendLineSeparator);
        assertThat(formatter.toJson(map)).isEqualToNormalizingNewlines(
                "{\n" +
                "  \"hobbies\" : [ \"Reading\", \"Biking\", \"Snorkeling\" ],\n" +
                "  \"name\" : \"Jim\"\n" +
                "}" + (appendLineSeparator ? "\n" : ""));
    }
}
