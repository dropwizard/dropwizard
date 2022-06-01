package io.dropwizard.jackson;

import static org.assertj.core.api.Assertions.assertThat;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilderSpec;
import com.google.common.net.HostAndPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GuavaExtrasModuleTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void setUp() throws Exception {
        mapper.registerModule(new GuavaModule());
        mapper.registerModule(new GuavaExtrasModule());
    }

    @Test
    void canDeserializeAHostAndPort() throws Exception {
        assertThat(mapper.readValue("\"example.com:8080\"", HostAndPort.class))
                .isEqualTo(HostAndPort.fromParts("example.com", 8080));
    }

    @Test
    void canDeserializeCacheBuilderSpecs() throws Exception {
        assertThat(mapper.readValue("\"maximumSize=30\"", CacheBuilderSpec.class))
                .isEqualTo(CacheBuilderSpec.parse("maximumSize=30"));
    }

    @Test
    void canSerializeCacheBuilderSpecs() throws Exception {
        assertThat(mapper.writeValueAsString(CacheBuilderSpec.disableCaching())).isEqualTo("\"maximumSize=0\"");
    }

    @Test
    void canDeserializeAbsentOptions() throws Exception {
        assertThat(mapper.readValue("null", Optional.class)).isEqualTo(Optional.absent());
    }

    @Test
    void canDeserializePresentOptions() throws Exception {
        assertThat(mapper.readValue("\"woo\"", Optional.class)).isEqualTo(Optional.of("woo"));
    }
}
