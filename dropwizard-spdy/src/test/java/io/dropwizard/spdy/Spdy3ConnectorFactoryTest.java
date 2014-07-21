package io.dropwizard.spdy;

import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class Spdy3ConnectorFactoryTest {
    @Test
    public void isDiscoverable() throws Exception {
        assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes())
                .contains(Spdy3ConnectorFactory.class);
    }
}
