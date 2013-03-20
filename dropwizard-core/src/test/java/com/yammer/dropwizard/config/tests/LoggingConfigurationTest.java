package com.yammer.dropwizard.config.tests;

import ch.qos.logback.classic.Level;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.yammer.dropwizard.config.ConfigurationFactory;
import com.yammer.dropwizard.config.LoggingConfiguration;
import com.yammer.dropwizard.validation.Validator;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

import static org.fest.assertions.api.Assertions.assertThat;

public class LoggingConfigurationTest {
    private final ConfigurationFactory<LoggingConfiguration> factory =
            ConfigurationFactory.forClass(LoggingConfiguration.class, new Validator());
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
