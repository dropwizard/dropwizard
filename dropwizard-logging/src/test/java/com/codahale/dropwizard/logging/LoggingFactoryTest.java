package com.codahale.dropwizard.logging;

import ch.qos.logback.classic.Level;
import com.codahale.dropwizard.configuration.ConfigurationFactory;
import com.codahale.dropwizard.jackson.Jackson;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;

import javax.validation.Validation;
import java.io.File;

import static org.fest.assertions.api.Assertions.assertThat;

public class LoggingFactoryTest {
    private final ObjectMapper objectMapper = Jackson.newObjectMapper();
    private final ConfigurationFactory<LoggingFactory> factory =
            new ConfigurationFactory<>(LoggingFactory.class,
                                       Validation.buildDefaultValidatorFactory().getValidator(),
                                       objectMapper, "dw");
    private LoggingFactory config;

    @Before
    public void setUp() throws Exception {
        objectMapper.getSubtypeResolver().registerSubtypes(ConsoleAppenderFactory.class,
                                                           FileAppenderFactory.class,
                                                           SyslogAppenderFactory.class);

        this.config = factory.build(new File(Resources.getResource("yaml/logging.yml").toURI()));
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
