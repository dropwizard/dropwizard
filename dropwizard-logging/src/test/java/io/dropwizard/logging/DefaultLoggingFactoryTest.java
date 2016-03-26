package io.dropwizard.logging;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.TextNode;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.configuration.FileConfigurationSourceProvider;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.logging.filter.FilterFactory;
import io.dropwizard.validation.BaseValidator;
import java.io.File;
import java.nio.charset.StandardCharsets;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.StrSubstitutor;
import org.assertj.core.data.MapEntry;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.LoggerFactory;

public class DefaultLoggingFactoryTest {
    private final ObjectMapper objectMapper = Jackson.newObjectMapper();
    private final ConfigurationFactory<DefaultLoggingFactory> factory = new ConfigurationFactory<>(
            DefaultLoggingFactory.class,
            BaseValidator.newValidator(),
            objectMapper, "dw");

    private DefaultLoggingFactory config;

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

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
        final AppenderFactory<ILoggingEvent> appenderFactory = newAppConfiguration.getAppenders().get(0);
        assertThat(appenderFactory).isInstanceOf(FileAppenderFactory.class);
        final FileAppenderFactory<ILoggingEvent> fileAppenderFactory = (FileAppenderFactory<ILoggingEvent>) appenderFactory;
        assertThat(fileAppenderFactory.getCurrentLogFilename()).isEqualTo("${new_app}.log");
        assertThat(fileAppenderFactory.getArchivedLogFilenamePattern()).isEqualTo("${new_app}-%d.log.gz");
        assertThat(fileAppenderFactory.getArchivedFileCount()).isEqualTo(5);
        final ImmutableList<FilterFactory<ILoggingEvent>> filterFactories = fileAppenderFactory.getFilterFactories();
        assertThat(filterFactories).hasSize(2);
        assertThat(filterFactories.get(0)).isExactlyInstanceOf(TestFilterFactory.class);
        assertThat(filterFactories.get(1)).isExactlyInstanceOf(SecondTestFilterFactory.class);

        final JsonNode legacyApp = config.getLoggers().get("com.example.legacyApp");
        assertThat(legacyApp).isNotNull();
        final LoggerConfiguration legacyAppConfiguration = objectMapper.treeToValue(legacyApp, LoggerConfiguration.class);
        assertThat(legacyAppConfiguration.getLevel()).isEqualTo(Level.DEBUG);
        // We should not create additional appenders, if they are not specified
        assertThat(legacyAppConfiguration.getAppenders()).isEmpty();
    }

    @Test
    public void testConfigure() throws Exception {
        final File newAppLog = folder.newFile("example-new-app.log");
        final File newAppNotAdditiveLog = folder.newFile("example-new-app-not-additive.log");
        final File defaultLog = folder.newFile("example.log");
        final StrSubstitutor substitutor = new StrSubstitutor(ImmutableMap.of(
                "new_app", StringUtils.removeEnd(newAppLog.getAbsolutePath(), ".log"),
                "new_app_not_additive", StringUtils.removeEnd(newAppNotAdditiveLog.getAbsolutePath(), ".log"),
                "default", StringUtils.removeEnd(defaultLog.getAbsolutePath(), ".log")
        ));

        final String configPath = Resources.getResource("yaml/logging_advanced.yml").getFile();
        final DefaultLoggingFactory config = factory.build(
                new SubstitutingSourceProvider(new FileConfigurationSourceProvider(), substitutor),
                configPath);
        config.configure(new MetricRegistry(), "test-logger");

        LoggerFactory.getLogger("com.example.app").debug("Application debug log");
        LoggerFactory.getLogger("com.example.app").info("Application log");
        LoggerFactory.getLogger("com.example.newApp").debug("New application debug log");
        LoggerFactory.getLogger("com.example.newApp").info("New application info log");
        LoggerFactory.getLogger("com.example.legacyApp").debug("Legacy application debug log");
        LoggerFactory.getLogger("com.example.legacyApp").info("Legacy application info log");
        LoggerFactory.getLogger("com.example.notAdditive").debug("Not additive application debug log");
        LoggerFactory.getLogger("com.example.notAdditive").info("Not additive application info log");

        config.stop();

        assertThat(Files.readLines(defaultLog, StandardCharsets.UTF_8)).containsOnly(
                "INFO  com.example.app: Application log",
                "DEBUG com.example.newApp: New application debug log",
                "INFO  com.example.newApp: New application info log",
                "DEBUG com.example.legacyApp: Legacy application debug log",
                "INFO  com.example.legacyApp: Legacy application info log");

        assertThat(Files.readLines(newAppLog, StandardCharsets.UTF_8)).containsOnly(
                "DEBUG com.example.newApp: New application debug log",
                "INFO  com.example.newApp: New application info log");

        assertThat(Files.readLines(newAppNotAdditiveLog, StandardCharsets.UTF_8)).containsOnly(
            "DEBUG com.example.notAdditive: Not additive application debug log",
            "INFO  com.example.notAdditive: Not additive application info log");
    }

}
