package io.dropwizard.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class DiscoverableSubtypeResolverTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final DiscoverableSubtypeResolver resolver = new DiscoverableSubtypeResolver(ExampleTag.class);

    @Before
    public void setUp() throws Exception {
        mapper.setSubtypeResolver(resolver);
    }

    @Test
    public void discoversSubtypes() throws Exception {
        assertThat(mapper.readValue("{\"type\":\"a\"}", ExampleSPI.class))
                .isInstanceOf(ImplA.class);

        assertThat(mapper.readValue("{\"type\":\"b\"}", ExampleSPI.class))
                .isInstanceOf(ImplB.class);
    }
}
