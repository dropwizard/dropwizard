package io.dropwizard.spdy;

import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import org.eclipse.jetty.spdy.server.http.PushStrategy;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
