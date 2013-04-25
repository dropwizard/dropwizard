package com.codahale.dropwizard.config;

import ch.qos.logback.classic.Level;
import com.codahale.dropwizard.configuration.ConfigurationFactory;
import com.codahale.dropwizard.jackson.Jackson;
import com.codahale.dropwizard.logging.ConsoleLoggingOutput;
import com.codahale.dropwizard.logging.FileLoggingOutput;
import com.codahale.dropwizard.logging.SyslogLoggingOutput;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;

import javax.validation.Validation;
import java.io.File;

import static org.fest.assertions.api.Assertions.assertThat;

public class LoggingConfigurationTest {
    private final ObjectMapper objectMapper = Jackson.newObjectMapper();
    private final ConfigurationFactory<LoggingConfiguration> factory =
            new ConfigurationFactory<>(LoggingConfiguration.class,
                                       Validation.buildDefaultValidatorFactory().getValidator(),
                                       objectMapper);
    private LoggingConfiguration config;

    @Before
    public void setUp() throws Exception {
        objectMapper.getSubtypeResolver().registerSubtypes(ConsoleLoggingOutput.class,
                                                           FileLoggingOutput.class,
                                                           SyslogLoggingOutput.class);

        this.config = factory.build(new File(Resources.getResource("logging.yml").toURI()));
    }

    @Test
    public void hasADefaultLevel() throws Exception {
        assertThat(config.getLevel())
                .isEqualTo(Level.INFO);
    }

    @Test
    public void hasASetOfOverriddenLevels() throws Exception {
        assertThat(config.getLoggers())
                .isEqualTo(ImmutableMap.of("com.example.app", Level.DEBUG));
    }
}
