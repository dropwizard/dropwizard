package io.dropwizard.metrics;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.logging.BootstrapLogging;
import io.dropwizard.util.Duration;
import io.dropwizard.validation.BaseValidator;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class MetricsFactoryTest {
    static {
        BootstrapLogging.bootstrap();
    }

    private final ObjectMapper objectMapper = Jackson.newObjectMapper();
    private final ConfigurationFactory<MetricsFactory> factory =
            new ConfigurationFactory<>(MetricsFactory.class,
                                       BaseValidator.newValidator(),
                                       objectMapper, "dw");
    private MetricsFactory config;

    @Before
    public void setUp() throws Exception {
        objectMapper.getSubtypeResolver().registerSubtypes(ConsoleReporterFactory.class,
                                                           CsvReporterFactory.class,
                                                           Slf4jReporterFactory.class);

        this.config = factory.build(new File(Resources.getResource("yaml/metrics.yml").toURI()));
    }

    @Test
    public void hasADefaultFrequency() throws Exception {
        assertThat(config.getFrequency())
                .isEqualTo(Duration.seconds(10));
    }

    @Test
    public void hasReporters() throws Exception {
        CsvReporterFactory csvReporter = new CsvReporterFactory();
        csvReporter.setFile(new File("metrics"));
        assertThat(config.getReporters()).hasSize(3);
    }
}
