package io.dropwizard.metrics.common;

import static org.assertj.core.api.Assertions.assertThat;

import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import org.junit.jupiter.api.Test;

class Slf4jReporterFactoryTest {
    @Test
    void isDiscoverable() throws Exception {
        assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes()).contains(Slf4jReporterFactory.class);
    }
}
