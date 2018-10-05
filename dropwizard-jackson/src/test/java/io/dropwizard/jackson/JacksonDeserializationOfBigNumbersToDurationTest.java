package io.dropwizard.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;

import javax.annotation.Nullable;
import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

public class JacksonDeserializationOfBigNumbersToDurationTest {

    private final ObjectMapper objectMapper = Jackson.newObjectMapper();

    @Test(timeout = 5000)
    public void testDoesNotAttemptToDeserializeExtremelyBigNumbers() {
        assertThatExceptionOfType(JsonMappingException.class).isThrownBy(
            () -> objectMapper.readValue("{\"id\": 42, \"duration\": 1e1000000000}", Task.class))
            .withMessageStartingWith("Value is out of range of Duration");
    }

    @Test
    public void testCanDeserializeZero() throws Exception {
        Task task = objectMapper.readValue("{\"id\": 42, \"duration\": 0}", Task.class);
        assertThat(task.getDuration()).isEqualTo(Duration.ofSeconds(0));
    }

    @Test
    public void testCanDeserializeNormalTimestamp() throws Exception {
        Task task = objectMapper.readValue("{\"id\": 42, \"duration\": 30}", Task.class);
        assertThat(task.getDuration()).isEqualTo(Duration.ofSeconds(30));
    }

    @Test
    public void testCanDeserializeNormalTimestampWithNanoseconds() throws Exception {
        Task task = objectMapper.readValue("{\"id\": 42, \"duration\": 30.314400507}", Task.class);
        assertThat(task.getDuration()).isEqualTo(Duration.ofSeconds(30, 314400507L));
    }

    @Test
    public void testCanDeserializeFromString() throws Exception {
        Task task = objectMapper.readValue("{\"id\": 42, \"duration\": \"PT30S\"}", Task.class);
        assertThat(task.getDuration()).isEqualTo(Duration.ofSeconds(30));
    }

    @Test
    public void testCanDeserializeMinDuration() throws Exception {
        Task task = objectMapper.readValue("{\"id\": 42, \"duration\": -9223372036854775808}", Task.class);
        assertThat(task.getDuration()).isEqualTo(Duration.ofSeconds(Long.MIN_VALUE));
    }

    @Test
    public void testCanDeserializeMaxDuration() throws Exception {
        Task task = objectMapper.readValue("{\"id\": 42, \"duration\": 9223372036854775807}", Task.class);
        assertThat(task.getDuration()).isEqualTo(Duration.ofSeconds(Long.MAX_VALUE));
    }

    @Test
    public void testCanNotDeserializeValueMoreThanMaxDuration() {
        assertThatExceptionOfType(JsonMappingException.class).isThrownBy(
            () -> objectMapper.readValue("{\"id\": 42, \"duration\": 9223372036854775808}", Task.class));
    }

    @Test
    public void testCanNotDeserializeValueLessThanMinDuration() {
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
