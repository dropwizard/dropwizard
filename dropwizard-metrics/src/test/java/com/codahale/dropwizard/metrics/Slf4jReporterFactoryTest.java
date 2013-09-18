package com.codahale.dropwizard.metrics;

import com.codahale.dropwizard.jackson.DiscoverableSubtypeResolver;
import com.codahale.dropwizard.metrics.Slf4jReporterFactory;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class Slf4jReporterFactoryTest {
    @Test
    public void isDiscoverable() throws Exception {
        assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes())
                .contains(Slf4jReporterFactory.class);
    }
}
