package io.dropwizard.metrics.ganglia;

import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import org.assertj.core.api.Assertions;
import org.junit.Test;

public class GangliaReporterFactoryTest {
    @Test
    public void isDiscoverable() throws Exception {
        Assertions.assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes())
                .contains(GangliaReporterFactory.class);
    }
}
