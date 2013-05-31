package com.codahale.dropwizard.metrics.reporters.ganglia;

import com.codahale.dropwizard.jackson.DiscoverableSubtypeResolver;
import com.codahale.dropwizard.metrics.ganglia.GangliaReporterFactory;
import org.fest.assertions.api.Assertions;
import org.junit.Test;

public class GangliaReporterFactoryTest {
    @Test
    public void isDiscoverable() throws Exception {
        Assertions.assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes())
                .contains(GangliaReporterFactory.class);
    }
}
