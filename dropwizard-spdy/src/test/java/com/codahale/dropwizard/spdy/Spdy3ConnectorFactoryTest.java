package com.codahale.dropwizard.spdy;

import com.codahale.dropwizard.jackson.DiscoverableSubtypeResolver;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class Spdy3ConnectorFactoryTest {
    @Test
    public void isDiscoverable() throws Exception {
        assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes())
                .contains(Spdy3ConnectorFactory.class);
    }
}
