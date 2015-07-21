package io.dropwizard.logging;

import static org.assertj.core.api.Assertions.assertThat;
import io.dropwizard.configuration.ConfigurationException;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.jackson.Jackson;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import javax.validation.Validation;

import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.classic.Level;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;

public class DefaultLoggingFactoryTest {
    private final ObjectMapper objectMapper = Jackson.newObjectMapper();
    private final ConfigurationFactory<DefaultLoggingFactory> factory = new ConfigurationFactory<>(
            DefaultLoggingFactory.class, Validation
                    .buildDefaultValidatorFactory().getValidator(),
            objectMapper, "dw");
    private DefaultLoggingFactory config;

    @Before
    public void setUp() throws Exception {
        objectMapper.getSubtypeResolver().registerSubtypes(
                ConsoleAppenderFactory.class, FileAppenderFactory.class,
                SyslogAppenderFactory.class);

        this.config = factory.build(new File(Resources.getResource(
                "yaml/logging.yml").toURI()));
    }

    @Test
    public void hasADefaultLevel() throws Exception {
        assertThat(config.getLevel()).isEqualTo(Level.INFO);
    }

    @Test
    public void canParseNewLoggerFormat() throws IOException,
            ConfigurationException, URISyntaxException {
        DefaultLoggingFactory config = factory.build(new File(Resources
                .getResource("yaml/logging_advanced.yml").toURI()));
        assertThat(config.getLoggers().get("com.example.newApp")).isNotNull();
        DefaultLoggerFactory configuration = Jackson.newObjectMapper()
                .treeToValue(config.getLoggers().get("com.example.newApp"),
                        DefaultLoggerFactory.class);
        assertThat(configuration.getLevel()).isEqualTo(Level.DEBUG);
        assertThat(configuration.getAppenders().get(0).getClass()).isEqualTo(
                ConsoleAppenderFactory.class);
        assertThat(configuration.getAppenders().get(1).getClass()).isEqualTo(
                FileAppenderFactory.class);
    }
}
