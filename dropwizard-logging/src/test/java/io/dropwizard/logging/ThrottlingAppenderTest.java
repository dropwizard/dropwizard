package io.dropwizard.logging;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
import io.dropwizard.configuration.ConfigurationParsingException;
import io.dropwizard.configuration.ConfigurationValidationException;
import io.dropwizard.configuration.FileConfigurationSourceProvider;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.validation.BaseValidator;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StrSubstitutor;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

public class ThrottlingAppenderTest {
    private final ObjectMapper objectMapper = Jackson.newObjectMapper();
    private final YamlConfigurationFactory<DefaultLoggingFactory> factory = new YamlConfigurationFactory<>(
        DefaultLoggingFactory.class,
        BaseValidator.newValidator(),
        objectMapper, "dw");


    private static File loadResource(String resourceName) throws URISyntaxException {
        return new File(Resources.getResource(resourceName).toURI());
    }

    @Test(expected = ConfigurationValidationException.class)
    public void appenderWithZeroThrottle() throws Exception {
        final YamlConfigurationFactory<ConsoleAppenderFactory> factory = new YamlConfigurationFactory<>(
            ConsoleAppenderFactory.class, BaseValidator.newValidator(), Jackson.newObjectMapper(), "dw");
        final ConsoleAppenderFactory appender = factory.build(loadResource("yaml/appender_with_zero_throttling.yml"));
    }

    @Test(expected = ConfigurationValidationException.class)
    public void appenderWithInvalidThrottle() throws Exception {
        final YamlConfigurationFactory<ConsoleAppenderFactory> factory = new YamlConfigurationFactory<>(
            ConsoleAppenderFactory.class, BaseValidator.newValidator(), Jackson.newObjectMapper(), "dw");
        final ConsoleAppenderFactory appender = factory.build(loadResource("yaml/appender_with_invalid_throttling.yml"));
    }

    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private File newLog() throws IOException {
        return folder.newFile("throttling.log");
    }

    private DefaultLoggingFactory setup(File defaultLog, String messageThrottle) throws Exception {
        StrSubstitutor substitutor = new StrSubstitutor(ImmutableMap.of(
            "default", StringUtils.removeEnd(defaultLog.getAbsolutePath(), ".log"),
            "messageThrottle", messageThrottle
        ));
        DefaultLoggingFactory config = factory.build(
            new SubstitutingSourceProvider(new FileConfigurationSourceProvider(), substitutor),
            loadResource("yaml/logging-throttling.yml").getPath());
        config.configure(new MetricRegistry(), "test-logger");
        return config;
    }

    @Test
    public void overThrottlingLimit() throws Exception {
        File defaultLog = newLog();
        DefaultLoggingFactory config = setup(defaultLog, "100ms");
        Logger logger = LoggerFactory.getLogger("com.example.app");
        Thread.sleep(1000);
        for (int i = 0; i < 100; i++) {
            logger.info("Application log {}", i);
        }
        config.stop();
        assertThat(Files.readAllLines(defaultLog.toPath())).containsOnly(
            "INFO  com.example.app: Application log 0",
            "INFO  com.example.app: Application log 1",
            "INFO  com.example.app: Application log 2",
            "INFO  com.example.app: Application log 3",
            "INFO  com.example.app: Application log 4",
            "INFO  com.example.app: Application log 5",
            "INFO  com.example.app: Application log 6",
            "INFO  com.example.app: Application log 7",
            "INFO  com.example.app: Application log 8",
            "INFO  com.example.app: Application log 9",
            "INFO  com.example.app: Application log 10");
    }

    @Test
    public void belowThrottlingLimit() throws Exception {
        File defaultLog = newLog();
        DefaultLoggingFactory config = setup(defaultLog, "1ms");
        Logger logger = LoggerFactory.getLogger("com.example.app");
        Thread.sleep(1000);
        for (int i = 0; i < 1000; i++) {
            logger.info("Application log {}", i);
        }
        config.stop();
        assertThat(Files.readAllLines(defaultLog.toPath())).size().isEqualTo(1000);
    }

    @Test
    public void overThrottlingLimit2Seconds() throws Exception {
        File defaultLog = newLog();
        DefaultLoggingFactory config = setup(defaultLog, "100ms");
        Logger logger = LoggerFactory.getLogger("com.example.app");
        Thread.sleep(1000);
        for (int i = 0; i < 100; i++) {
            if (i == 50) {
                Thread.sleep(1000);
            }
            logger.info("Application log {}", i);
        }
        config.stop();
        List<String> lines = Files.readAllLines(defaultLog.toPath());
        assertThat(lines).hasSize(21);
        assertThat(lines.get(0)).isEqualTo("INFO  com.example.app: Application log 0");
        assertThat(lines.get(20)).isEqualTo("INFO  com.example.app: Application log 59");
    }

}
