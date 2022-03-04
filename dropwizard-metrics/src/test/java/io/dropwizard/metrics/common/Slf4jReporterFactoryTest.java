package io.dropwizard.metrics.common;

import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.metrics.common.Slf4jReporterFactory;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class Slf4jReporterFactoryTest {
    @Test
    void isDiscoverable() throws Exception {
        assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes())
                .contains(Slf4jReporterFactory.class);
    }
}
