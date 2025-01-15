package io.dropwizard.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertTimeout;


class JacksonDeserializationOfBigNumbersToDurationTest {

    private final ObjectMapper objectMapper = Jackson.newObjectMapper();

    @Test
    void testDoesNotAttemptToDeserializeExtremelyBigNumbers() throws Exception {
        Task task = objectMapper.readValue("{\"id\": 42, \"duration\": 1e1000000000}", Task.class);
        assertTimeout(Duration.ofSeconds(5L), () -> assertThat(task.getDuration()).isEqualTo(Duration.ofSeconds(0)));
    }

    @Test
    void testCanDeserializeZero() throws Exception {
        Task task = objectMapper.readValue("{\"id\": 42, \"duration\": 0}", Task.class);
        assertThat(task.getDuration()).isEqualTo(Duration.ofSeconds(0));
    }

    @Test
    void testCanDeserializeNormalTimestamp() throws Exception {
        Task task = objectMapper.readValue("{\"id\": 42, \"duration\": 30}", Task.class);
        assertThat(task.getDuration()).isEqualTo(Duration.ofSeconds(30));
    }

    @Test
    void testCanDeserializeNormalTimestampWithNanoseconds() throws Exception {
        Task task = objectMapper.readValue("{\"id\": 42, \"duration\": 30.314400507}", Task.class);
        assertThat(task.getDuration()).isEqualTo(Duration.ofSeconds(30, 314400507L));
    }

    @Test
    void testCanDeserializeFromString() throws Exception {
        Task task = objectMapper.readValue("{\"id\": 42, \"duration\": \"PT30S\"}", Task.class);
        assertThat(task.getDuration()).isEqualTo(Duration.ofSeconds(30));
    }

    @Test
    void testCanDeserializeMinDuration() throws Exception {
        Task task = objectMapper.readValue("{\"id\": 42, \"duration\": -9223372036854775808}", Task.class);
        assertThat(task.getDuration()).isEqualTo(Duration.ofSeconds(Long.MIN_VALUE));
    }

    @Test
    void testCanDeserializeMaxDuration() throws Exception {
        Task task = objectMapper.readValue("{\"id\": 42, \"duration\": 9223372036854775807}", Task.class);
        assertThat(task.getDuration()).isEqualTo(Duration.ofSeconds(Long.MAX_VALUE));
    }

    @Test
    void testCanNotDeserializeValueMoreThanMaxDuration() {
        assertThatExceptionOfType(JsonMappingException.class).isThrownBy(
            () -> objectMapper.readValue("{\"id\": 42, \"duration\": 9223372036854775808}", Task.class));
    }

    @Test
    void testCanNotDeserializeValueLessThanMinDuration() {
        assertThatExceptionOfType(JsonMappingException.class).isThrownBy(
            () -> objectMapper.readValue("{\"id\": 42, \"duration\": -9223372036854775809}", Task.class));
    }

    static class Task {

        private int id;
        @Nullable
        private Duration duration;

        @JsonProperty
        int getId() {
            return id;
        }

        @JsonProperty
        void setId(int id) {
            this.id = id;
        }

        @JsonProperty
        @Nullable
        Duration getDuration() {
            return duration;
        }

        @JsonProperty
        void setDuration(Duration duration) {
            this.duration = duration;
        }
    }

}
