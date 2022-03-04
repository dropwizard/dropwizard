package io.dropwizard.metrics.common;

import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.metrics.common.ConsoleReporterFactory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ConsoleReporterFactoryTest {
    @Test
    void isDiscoverable() throws Exception {
        assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes())
                .contains(ConsoleReporterFactory.class);
    }
}
