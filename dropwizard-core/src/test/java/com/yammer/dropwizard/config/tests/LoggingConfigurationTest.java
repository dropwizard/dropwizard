package com.yammer.dropwizard.config.tests;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.junit.Assert.assertEquals;

import java.io.File;

import org.junit.Before;
import org.junit.Test;

import ch.qos.logback.classic.Level;

import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import com.yammer.dropwizard.config.ConfigurationFactory;
import com.yammer.dropwizard.config.LoggingConfiguration;
import com.yammer.dropwizard.config.LoggingConfiguration.ConsoleConfiguration;
import com.yammer.dropwizard.config.LoggingConfiguration.FileConfiguration;
import com.yammer.dropwizard.validation.Validator;

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

    @Test
    public void hasConsoleConfiguration() throws Exception {
        final ConsoleConfiguration console = config.getConsoleConfiguration();

        assertThat(console.isEnabled())
                .isTrue();

        assertThat(console.getThreshold())
                .isEqualTo(Level.ALL);
    }

	@Test
	public void hasFileConfiguration() throws Exception {
		
		assertEquals(2, config.getFileLoggers().size());
		
		for (FileConfiguration file : config.getFileLoggers().values()) {
			assertThat(file.isEnabled()).isFalse();

			assertThat(file.getThreshold()).isEqualTo(Level.ALL);

			assertThat(file.isArchive()).isTrue();

			assertThat(file.getCurrentLogFilename()).isEqualTo(
					"./logs/example.log");

			assertThat(file.getArchivedLogFilenamePattern()).isEqualTo(
					"./logs/example-%d.log.gz");

			assertThat(file.getArchivedFileCount()).isEqualTo(5);
		}
	}

    @Test
    public void defaultFileConfigurationIsValid() throws Exception {
        final FileConfiguration file = new FileConfiguration();

        assertThat(file.isValidArchiveConfiguration())
                .isTrue();
    }
}
