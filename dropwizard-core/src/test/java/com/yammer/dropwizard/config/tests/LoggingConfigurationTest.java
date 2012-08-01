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

import static com.yammer.dropwizard.config.LoggingConfiguration.ConsoleConfiguration;
import static com.yammer.dropwizard.config.LoggingConfiguration.FileConfiguration;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

public class LoggingConfigurationTest {
    private final ConfigurationFactory<LoggingConfiguration> factory =
            ConfigurationFactory.forClass(LoggingConfiguration.class, new Validator());
    private LoggingConfiguration config;

    @Before
    public void setUp() throws Exception {
        this.config = factory.build(new File(Resources.getResource("logging.yml")
                                                      .getFile()));
    }

    @Test
    public void hasADefaultLevel() throws Exception {
        assertThat(config.getLevel(),
                   is(Level.INFO));
    }

    @Test
    public void hasASetOfOverriddenLevels() throws Exception {
        assertThat(config.getLoggers(),
                   is(ImmutableMap.of("com.example.app", Level.DEBUG)));
    }

    @Test
    public void hasConsoleConfiguration() throws Exception {
        final ConsoleConfiguration console = config.getConsoleConfiguration();
        
        assertThat(console.isEnabled(),
                   is(true));
        
        assertThat(console.getThreshold(),
                   is(Level.ALL));
    }

    @Test
    public void hasFileConfiguration() throws Exception {
        final FileConfiguration file = config.getFileConfiguration();

        assertThat(file.isEnabled(),
                   is(false));
        
        assertThat(file.getThreshold(),
                   is(Level.ALL));
        
        assertThat(file.isArchive(),
                   is(true));
        
        assertThat(file.getCurrentLogFilename(),
                   is("./logs/example.log"));

        assertThat(file.getArchivedLogFilenamePattern(),
                   is("./logs/example-%d.log.gz"));

        assertThat(file.getArchivedFileCount(),
                   is(5));
    }
    
    @Test
    public void defaultFileConfigurationIsValid() throws Exception {
        final FileConfiguration file = new FileConfiguration();
        
        assertThat(file.isValidArchiveConfiguration(),
                   is(true));
    }
}
