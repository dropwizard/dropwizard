package com.codahale.dropwizard.metrics;

import com.codahale.dropwizard.jackson.DiscoverableSubtypeResolver;
import com.codahale.dropwizard.metrics.CsvReporterFactory;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class CsvReporterFactoryTest {
    @Test
    public void isDiscoverable() throws Exception {
        assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes())
                .contains(CsvReporterFactory.class);
    }
}
