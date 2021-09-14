package io.dropwizard.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DiscoverableSubtypeResolverTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final DiscoverableSubtypeResolver resolver = new DiscoverableSubtypeResolver(ExampleTag.class);

    @BeforeEach
    void setUp() throws Exception {
        mapper.setSubtypeResolver(resolver);
    }

    @Test
    void discoversSubtypes() throws Exception {
        assertThat(mapper.readValue("{\"type\":\"a\"}", ExampleSPI.class))
                .isInstanceOf(ImplA.class);

        assertThat(mapper.readValue("{\"type\":\"b\"}", ExampleSPI.class))
                .isInstanceOf(ImplB.class);
    }
}
