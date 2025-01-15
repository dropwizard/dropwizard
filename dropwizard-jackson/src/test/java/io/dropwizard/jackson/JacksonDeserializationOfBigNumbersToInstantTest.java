package io.dropwizard.jackson;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.junit.jupiter.api.Assertions.assertTimeout;

class JacksonDeserializationOfBigNumbersToInstantTest {

    private final ObjectMapper objectMapper = Jackson.newObjectMapper();

    @Test
    void testDoesNotAttemptToDeserializeExtremelBigNumbers() throws Exception {
        Event event = objectMapper.readValue("{\"id\": 42, \"createdAt\": 1e1000000000}", Event.class);
        assertTimeout(Duration.ofSeconds(5L), () -> assertThat(event.getCreatedAt()).isEqualTo(Instant.ofEpochMilli(0)));
    }

    @Test
    void testCanDeserializeZero() throws Exception {
        Event event = objectMapper.readValue("{\"id\": 42, \"createdAt\": 0}", Event.class);
        assertThat(event.getCreatedAt()).isEqualTo(Instant.ofEpochMilli(0));
    }

    @Test
    void testCanDeserializeNormalTimestamp() throws Exception {
        Event event = objectMapper.readValue("{\"id\": 42, \"createdAt\": 1538326357}", Event.class);
        assertThat(event.getCreatedAt()).isEqualTo(Instant.ofEpochMilli(1538326357000L));
    }

    @Test
    void testCanDeserializeNormalTimestampWithNanoseconds() throws Exception {
        Event event = objectMapper.readValue("{\"id\": 42, \"createdAt\": 1538326357.298509112}", Event.class);
        assertThat(event.getCreatedAt()).isEqualTo(Instant.ofEpochSecond(1538326357, 298509112L));
    }

    @Test
    void testCanDeserializeMinInstant() throws Exception {
        Event event = objectMapper.readValue("{\"id\": 42, \"createdAt\": -31557014167219200}", Event.class);
        assertThat(event.getCreatedAt()).isEqualTo(Instant.MIN);
    }

    @Test
    void testCanDeserializeMaxInstant() throws Exception {
        Event event = objectMapper.readValue("{\"id\": 42, \"createdAt\": 31556889864403199.999999999}", Event.class);
        assertThat(event.getCreatedAt()).isEqualTo(Instant.MAX);
    }

    @Test
    void testCanNotDeserializeValueMoreThanMaxInstant() {
        assertThatExceptionOfType(JsonMappingException.class).isThrownBy(
            () -> objectMapper.readValue("{\"id\": 42, \"createdAt\": 31556889864403200}", Event.class));
    }

    @Test
    void testCanNotDeserializeValueLessThanMaxInstant() {
        assertThatExceptionOfType(JsonMappingException.class).isThrownBy(
            () -> objectMapper.readValue("{\"id\": 42, \"createdAt\": -31557014167219201}", Event.class));
    }

    static class Event {

        private int id;
        @Nullable
        private Instant createdAt;

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
        Instant getCreatedAt() {
            return createdAt;
        }

        @JsonProperty
        void setCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
        }
    }

}
