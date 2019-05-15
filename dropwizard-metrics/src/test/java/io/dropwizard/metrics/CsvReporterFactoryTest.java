package io.dropwizard.metrics;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.util.Resources;
import io.dropwizard.validation.BaseValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class CsvReporterFactoryTest {
    private final ObjectMapper objectMapper = Jackson.newObjectMapper();
    private final YamlConfigurationFactory<MetricsFactory> factory =
            new YamlConfigurationFactory<>(MetricsFactory.class,
                                           BaseValidator.newValidator(),
                                           objectMapper, "dw");

    @BeforeEach
    public void setUp() throws Exception {
        objectMapper.getSubtypeResolver().registerSubtypes(ConsoleReporterFactory.class,
                                                           CsvReporterFactory.class,
                                                           Slf4jReporterFactory.class);
    }

    @Test
    public void isDiscoverable() throws Exception {
        assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes())
                .contains(CsvReporterFactory.class);
    }

    @Test
    public void directoryCreatedOnStartup() throws Exception {
        File dir = new File("metrics");
        dir.delete();

        MetricsFactory config = factory.build(new File(Resources.getResource("yaml/metrics.yml").toURI()));
        MetricRegistry metricRegistry = new MetricRegistry();
        config.configure(new LifecycleEnvironment(metricRegistry), metricRegistry);
        assertThat(dir.exists()).isEqualTo(true);
    }
}
