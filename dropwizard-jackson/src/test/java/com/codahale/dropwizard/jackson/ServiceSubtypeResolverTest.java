package com.codahale.dropwizard.jackson;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class ServiceSubtypeResolverTest {
    private final ObjectMapper mapper = new ObjectMapper();
    private final ServiceSubtypeResolver resolver = new ServiceSubtypeResolver(ExampleTag.class);

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
