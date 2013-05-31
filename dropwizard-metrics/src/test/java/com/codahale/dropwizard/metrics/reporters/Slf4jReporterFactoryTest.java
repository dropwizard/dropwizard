package com.codahale.dropwizard.metrics.reporters;

import com.codahale.dropwizard.jackson.DiscoverableSubtypeResolver;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Tests {@link Slf4jReporterFactory}.
 */
public class Slf4jReporterFactoryTest {
    @Test
    public void isDiscoverable() throws Exception {
        assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes())
                .contains(Slf4jReporterFactory.class);
    }
}
