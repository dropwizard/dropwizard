package com.codahale.dropwizard.config.tests;

import ch.qos.logback.classic.Level;
import com.codahale.dropwizard.config.ConfigurationFactory;
import com.codahale.dropwizard.config.LoggingConfiguration;
import com.codahale.dropwizard.jackson.Jackson;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import org.junit.Before;
import org.junit.Test;

import javax.validation.Validation;
import java.io.File;

import static org.fest.assertions.api.Assertions.assertThat;

public class LoggingConfigurationTest {
    private final ConfigurationFactory<LoggingConfiguration> factory =
            new ConfigurationFactory<>(LoggingConfiguration.class,
                                       Validation.buildDefaultValidatorFactory().getValidator(),
                                       Jackson.newObjectMapper());
    private LoggingConfiguration config;

    @Before
    public void setUp() throws Exception {
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
