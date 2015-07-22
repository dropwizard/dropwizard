package io.dropwizard.logging;

import ch.qos.logback.classic.Level;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.base.Charsets;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import org.assertj.core.data.MapEntry;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import javax.validation.Validation;
import java.io.File;

import static org.assertj.core.api.Assertions.assertThat;

public class DefaultLoggingFactoryTest {
    private final ObjectMapper objectMapper = Jackson.newObjectMapper();
    private final ConfigurationFactory<DefaultLoggingFactory> factory = new ConfigurationFactory<>(
            DefaultLoggingFactory.class,
            Validation.buildDefaultValidatorFactory().getValidator(),
            objectMapper, "dw");

    private DefaultLoggingFactory config;

    @Before
    public void setUp() throws Exception {
        objectMapper.getSubtypeResolver().registerSubtypes(ConsoleAppenderFactory.class,
                FileAppenderFactory.class,
                SyslogAppenderFactory.class);

        config = factory.build(new File(Resources.getResource("yaml/logging.yml").toURI()));
    }

    @Test
    public void hasADefaultLevel() throws Exception {
        assertThat(config.getLevel()).isEqualTo(Level.INFO);
    }

    @Test
    public void canParseNewLoggerFormat() throws Exception {
        final DefaultLoggingFactory config = factory.build(
                new File(Resources.getResource("yaml/logging_advanced.yml").toURI()));

        assertThat(config.getLoggers()).contains(MapEntry.entry("com.example.app", new TextNode("INFO")));

        final JsonNode newApp = config.getLoggers().get("com.example.newApp");
        assertThat(newApp).isNotNull();
        final LoggerConfiguration newAppConfiguration = objectMapper.treeToValue(newApp, LoggerConfiguration.class);
        assertThat(newAppConfiguration.getLevel()).isEqualTo(Level.DEBUG);
        assertThat(newAppConfiguration.getAppenders()).hasSize(1);
        final AppenderFactory appenderFactory = newAppConfiguration.getAppenders().get(0);
        assertThat(appenderFactory).isInstanceOf(FileAppenderFactory.class);
        final FileAppenderFactory fileAppenderFactory = (FileAppenderFactory) appenderFactory;
        assertThat(fileAppenderFactory.getCurrentLogFilename()).isEqualTo("/tmp/example-new-app.log");
        assertThat(fileAppenderFactory.getArchivedLogFilenamePattern()).isEqualTo("/tmp/example-new-app-%d.log.gz");
        assertThat(fileAppenderFactory.getArchivedFileCount()).isEqualTo(5);

        final JsonNode legacyApp = config.getLoggers().get("com.example.legacyApp");
        assertThat(legacyApp).isNotNull();
        final LoggerConfiguration legacyAppConfiguration = objectMapper.treeToValue(legacyApp, LoggerConfiguration.class);
        assertThat(legacyAppConfiguration.getLevel()).isEqualTo(Level.DEBUG);
        // We should not create additional appenders, if they are not specified
        assertThat(legacyAppConfiguration.getAppenders()).isEmpty();
    }

    @Test
    public void testConfigure() throws Exception {
        final File newAppLog = new File("/tmp/example-new-app.log");
        final File defaultLog = new File("/tmp/example.log");
        Files.write(new byte[]{}, newAppLog);
        Files.write(new byte[]{}, defaultLog);

        final DefaultLoggingFactory config = factory.build(
                new File(Resources.getResource("yaml/logging_advanced.yml").toURI()));
        config.configure(new MetricRegistry(), "test-logger");

        LoggerFactory.getLogger("com.example.app").debug("Application debug log");
        LoggerFactory.getLogger("com.example.app").info("Application log");
        LoggerFactory.getLogger("com.example.newApp").debug("New application debug log");
        LoggerFactory.getLogger("com.example.newApp").info("New application info log");
        LoggerFactory.getLogger("com.example.legacyApp").debug("Legacy application debug log");
        LoggerFactory.getLogger("com.example.legacyApp").info("Legacy application info log");

        config.stop();

        assertThat(Files.readLines(defaultLog, Charsets.UTF_8)).containsOnly(
                "INFO  com.example.app: Application log",
                "DEBUG com.example.newApp: New application debug log",
                "INFO  com.example.newApp: New application info log",
                "DEBUG com.example.legacyApp: Legacy application debug log",
                "INFO  com.example.legacyApp: Legacy application info log");

        assertThat(Files.readLines(newAppLog, Charsets.UTF_8)).containsOnly(
                "DEBUG com.example.newApp: New application debug log",
                "INFO  com.example.newApp: New application info log");

    }
}
