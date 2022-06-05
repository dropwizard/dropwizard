package io.dropwizard.metrics;

import com.codahale.metrics.MetricAttribute;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.DefaultObjectMapperFactory;
import io.dropwizard.logging.BootstrapLogging;
import io.dropwizard.util.Duration;
import io.dropwizard.validation.BaseValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.EnumSet;

import static org.assertj.core.api.Assertions.assertThat;

class MetricsFactoryTest {
    static {
        BootstrapLogging.bootstrap();
    }

    private final ObjectMapper objectMapper = new DefaultObjectMapperFactory().newObjectMapper();
    private final YamlConfigurationFactory<MetricsFactory> factory = new YamlConfigurationFactory<>(
        MetricsFactory.class, BaseValidator.newValidator(), objectMapper, "dw");
    private MetricsFactory config;

    @BeforeEach
    void setUp() throws Exception {
        objectMapper.getSubtypeResolver().registerSubtypes(ConsoleReporterFactory.class, CsvReporterFactory.class,
            Slf4jReporterFactory.class);

        this.config = factory.build(new ResourceConfigurationSourceProvider(), "yaml/metrics.yml");
    }

    @Test
    void hasADefaultFrequency() {
        assertThat(config.getFrequency()).isEqualTo(Duration.seconds(10));
    }

    @Test
    void hasReporters() {
        CsvReporterFactory csvReporter = new CsvReporterFactory();
        csvReporter.setFile(new File("metrics"));
        assertThat(config.getReporters()).hasSize(3);
    }

    @Test
    void canReadExcludedAndIncludedAttributes() {
        assertThat(config.getReporters())
            .hasSize(3)
            .element(0)
            .isInstanceOfSatisfying(ConsoleReporterFactory.class, consoleReporterFactory -> assertThat(consoleReporterFactory)
                .satisfies(factory -> assertThat(factory.getIncludesAttributes())
                    .isEqualTo(EnumSet.of(MetricAttribute.P50, MetricAttribute.P95, MetricAttribute.P98, MetricAttribute.P99)))
                    .satisfies(factory -> assertThat(factory.getExcludesAttributes()).isEqualTo(EnumSet.of(MetricAttribute.P98))));
    }

    @Test
    void canReadDefaultExcludedAndIncludedAttributes() {
        assertThat(config.getReporters())
            .hasSize(3)
            .element(1)
            .isInstanceOfSatisfying(CsvReporterFactory.class, csvReporterFactory -> assertThat(csvReporterFactory)
                .satisfies(factory -> assertThat(factory.getIncludesAttributes()).isEqualTo(EnumSet.allOf(MetricAttribute.class)))
                .satisfies(factory -> assertThat(factory.getExcludesAttributes()).isEmpty()));
    }

    @Test
    void reportOnStopFalseByDefault() {
        assertThat(config.isReportOnStop()).isFalse();
    }

    @Test
    void reportOnStopCanBeTrue() throws Exception {
        config = factory.build(new ResourceConfigurationSourceProvider(), "yaml/metrics-report-on-stop.yml");
        assertThat(config.isReportOnStop()).isTrue();
    }

}
