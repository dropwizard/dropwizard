package com.codahale.dropwizard.spdy;

import com.codahale.dropwizard.jackson.DiscoverableSubtypeResolver;
import org.eclipse.jetty.spdy.server.http.PushStrategy;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class NonePushStrategyFactoryTest {
    private final NonePushStrategyFactory factory = new NonePushStrategyFactory();

    @Test
    public void returnsAPushStrategyWhichNeverPushesAnything() throws Exception {
        assertThat(factory.build())
                .isInstanceOf(PushStrategy.None.class);
    }

    @Test
    public void isDiscoverable() throws Exception {
        assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes())
                .contains(NonePushStrategyFactory.class);
    }
}
