package io.dropwizard.metrics.ganglia;

import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.validation.BaseValidator;
import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

public class GangliaReporterFactoryTest {

    @Test
    public void isDiscoverable() throws Exception {
        assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes())
            .contains(GangliaReporterFactory.class);
    }

    @Test
    public void createDefaultFactory() throws Exception {
        final GangliaReporterFactory factory = new YamlConfigurationFactory<>(GangliaReporterFactory.class,
            BaseValidator.newValidator(), Jackson.newObjectMapper(), "dw")
            .build();
        assertThat(factory.getFrequency()).isEqualTo(Optional.empty());
    }
}
