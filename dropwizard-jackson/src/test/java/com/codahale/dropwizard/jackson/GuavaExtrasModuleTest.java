package com.codahale.dropwizard.jackson;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.guava.GuavaModule;
import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilderSpec;
import com.google.common.net.HostAndPort;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class GuavaExtrasModuleTest {
    private final ObjectMapper mapper = new ObjectMapper();

    @Before
    public void setUp() throws Exception {
        mapper.registerModule(new GuavaModule());
        mapper.registerModule(new GuavaExtrasModule());
    }

    @Test
    public void canDeserializeAHostAndPort() throws Exception {
        assertThat(mapper.readValue("\"example.com:8080\"", HostAndPort.class))
                .isEqualTo(HostAndPort.fromParts("example.com", 8080));
    }

    @Test
    public void canDeserializeCacheBuilderSpecs() throws Exception {
        assertThat(mapper.readValue("\"maximumSize=30\"", CacheBuilderSpec.class))
                .isEqualTo(CacheBuilderSpec.parse("maximumSize=30"));
    }

    @Test
    public void canDeserializeAbsentOptions() throws Exception {
        assertThat(mapper.readValue("null", new TypeReference<Optional<String>>() {}))
                .isEqualTo(Optional.absent());
    }

    @Test
    public void canDeserializePresentOptions() throws Exception {
        assertThat(mapper.readValue("\"woo\"", new TypeReference<Optional<String>>() {}))
                .isEqualTo(Optional.of("woo"));
    }
}
