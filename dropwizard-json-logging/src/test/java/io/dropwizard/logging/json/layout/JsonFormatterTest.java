package io.dropwizard.logging.json.layout;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.util.Maps;
import org.junit.Test;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class JsonFormatterTest {

    private final Map<String, Object> map = Maps.of(
            "name", "Jim",
            "hobbies", Arrays.asList("Reading", "Biking", "Snorkeling"));
    private final ObjectMapper objectMapper = Jackson.newObjectMapper();

    @Test
    public void testNoPrettyPrintNoLineSeparator() throws IOException {
        JsonFormatter formatter = new JsonFormatter(objectMapper, false, false);

        final JsonNode actual = objectMapper.readTree(formatter.toJson(map));
        final JsonNode expected = objectMapper.readTree("{\"name\":\"Jim\",\"hobbies\":[\"Reading\",\"Biking\",\"Snorkeling\"]}");
        assertThat(actual).isEqualTo(expected);
    }


    @Test
    public void testNoPrettyPrintWithLineSeparator() throws IOException {
        JsonFormatter formatter = new JsonFormatter(objectMapper, false, true);

        final String content = formatter.toJson(map);
        assertThat(content).endsWith(System.lineSeparator());
        final JsonNode actual = objectMapper.readTree(content);
        final JsonNode expected = objectMapper.readTree("{\"name\":\"Jim\",\"hobbies\":[\"Reading\",\"Biking\",\"Snorkeling\"]}");
        assertThat(actual).isEqualTo(expected);
    }

    @Test
    public void testPrettyPrintWithLineSeparator() {
        JsonFormatter formatter = new JsonFormatter(objectMapper, true, true);
        assertThat(formatter.toJson(map)).isEqualTo(String.format("{%n" +
                "  \"hobbies\" : [ \"Reading\", \"Biking\", \"Snorkeling\" ],%n" +
                "  \"name\" : \"Jim\"%n" +
                "}%n"));
    }

    @Test
    public void testPrettyPrintNoLineSeparator() {
        JsonFormatter formatter = new JsonFormatter(objectMapper, true, false);
        assertThat(formatter.toJson(map)).isEqualTo(String.format("{%n" +
                "  \"hobbies\" : [ \"Reading\", \"Biking\", \"Snorkeling\" ],%n" +
                "  \"name\" : \"Jim\"%n" +
                "}"));
    }
}
