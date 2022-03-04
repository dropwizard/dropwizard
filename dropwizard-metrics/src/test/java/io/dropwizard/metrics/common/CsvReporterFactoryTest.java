package io.dropwizard.metrics.common;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.metrics.common.ConsoleReporterFactory;
import io.dropwizard.metrics.common.CsvReporterFactory;
import io.dropwizard.metrics.common.MetricsFactory;
import io.dropwizard.metrics.common.Slf4jReporterFactory;
import io.dropwizard.validation.BaseValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

class CsvReporterFactoryTest {
    private final ObjectMapper objectMapper = Jackson.newObjectMapper();
    private final YamlConfigurationFactory<MetricsFactory> factory =
            new YamlConfigurationFactory<>(MetricsFactory.class,
                                           BaseValidator.newValidator(),
                                           objectMapper, "dw");

    @BeforeEach
    void setUp() {
        objectMapper.getSubtypeResolver().registerSubtypes(ConsoleReporterFactory.class,
                                                           CsvReporterFactory.class,
                                                           Slf4jReporterFactory.class);
    }

    @Test
    void isDiscoverable() {
        assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes())
                .contains(CsvReporterFactory.class);
    }

    @Test
    void directoryCreatedOnStartup() throws Exception {
        File dir = new File("metrics");
        dir.delete();
        assertThat(dir).doesNotExist();

        MetricsFactory config = factory.build(new ResourceConfigurationSourceProvider(), "yaml/metrics.yml");
        MetricRegistry metricRegistry = new MetricRegistry();
        config.configure(new LifecycleEnvironment(metricRegistry), metricRegistry);
        assertThat(dir).exists();
    }
}
