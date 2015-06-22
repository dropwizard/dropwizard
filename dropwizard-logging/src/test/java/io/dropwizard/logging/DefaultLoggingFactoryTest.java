package io.dropwizard.logging;

import ch.qos.logback.classic.Level;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;

import io.dropwizard.configuration.ConfigurationException;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.jackson.Jackson;

import org.junit.Before;
import org.junit.Test;

import javax.validation.Validation;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;

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
    public void hasASetOfOverriddenLevels() throws Exception {
        assertThat(config.getLoggers()).isEqualTo(
                ImmutableMap.of("com.example.app", Level.DEBUG));
    }

    @Test
    public void canReadAdvancedLoggers() throws IOException,
            ConfigurationException, URISyntaxException {
        DefaultLoggingFactory advancedConfig = factory.build(new File(Resources
                .getResource("yaml/logging_advanced.yml").toURI()));
        assertThat(advancedConfig.getAdvancedLoggers().get("com.example.app"))
                .isNotNull();
        assertThat(
                advancedConfig.getAdvancedLoggers().get("com.example.app")
                        .getLevel()).isEqualTo(Level.DEBUG);
        assertThat(
                advancedConfig.getAdvancedLoggers().get("com.example.app")
                        .getAppenders()).isNotNull();
        assertThat(
                advancedConfig.getAdvancedLoggers().get("com.example.app")
                        .getAppenders().get(0).getClass()).isEqualTo(
                ConsoleAppenderFactory.class);
        assertThat(
                advancedConfig.getAdvancedLoggers().get("com.example.app")
                        .getAppenders().get(1).getClass()).isEqualTo(
                FileAppenderFactory.class);
    }
}
