package com.codahale.dropwizard.metrics;

import com.codahale.dropwizard.configuration.ConfigurationFactory;
import com.codahale.dropwizard.jackson.Jackson;
import com.codahale.dropwizard.metrics.reporters.ConsoleReporterFactory;
import com.codahale.dropwizard.metrics.reporters.CsvReporterFactory;
import com.codahale.dropwizard.metrics.reporters.Slf4jReporterFactory;
import com.codahale.dropwizard.util.Duration;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableList;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;

import javax.validation.Validation;
import java.io.File;

import static org.fest.assertions.api.Assertions.assertThat;

public class MetricsFactoryTest {
    private final ObjectMapper objectMapper = Jackson.newObjectMapper();
    private final ConfigurationFactory<MetricsFactory> factory =
            new ConfigurationFactory<>(MetricsFactory.class,
                                       Validation.buildDefaultValidatorFactory().getValidator(),
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
        assertThat(config.getReporters()).isNotEmpty();
    }
}
