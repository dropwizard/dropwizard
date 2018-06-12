package io.dropwizard.logging;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.io.Resources;
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
import java.nio.file.Files;

import static org.assertj.core.api.Assertions.assertThat;

public class ThrottlingAppenderTest {
    private final ObjectMapper objectMapper = Jackson.newObjectMapper();
    private final YamlConfigurationFactory<DefaultLoggingFactory> factory = new YamlConfigurationFactory<>(
        DefaultLoggingFactory.class,
        BaseValidator.newValidator(),
        objectMapper, "dw");


    @Rule
    public TemporaryFolder folder = new TemporaryFolder();

    private File newLog() throws IOException {
        return folder.newFile("throttling.log");
    }

    private DefaultLoggingFactory setup(File defaultLog, double maxMessagesPerSecond) throws Exception {
        StrSubstitutor substitutor = new StrSubstitutor(ImmutableMap.of(
            "default", StringUtils.removeEnd(defaultLog.getAbsolutePath(), ".log"),
            "maxMessagesPerSecond", maxMessagesPerSecond
        ));
        String configPath = Resources.getResource("yaml/logging-throttling.yml").getFile();
        DefaultLoggingFactory config = factory.build(
            new SubstitutingSourceProvider(new FileConfigurationSourceProvider(), substitutor),
            configPath);
        config.configure(new MetricRegistry(), "test-logger");
        return config;
    }

    @Test
    public void overThrottlingLimit() throws Exception {
        File defaultLog = newLog();
        DefaultLoggingFactory config = setup(defaultLog, 10);
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
        DefaultLoggingFactory config = setup(defaultLog, 1000);
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
        DefaultLoggingFactory config = setup(defaultLog, 10);
        Logger logger = LoggerFactory.getLogger("com.example.app");
        Thread.sleep(1000);
        for (int i = 0; i < 100; i++) {
            if (i == 50) {
                Thread.sleep(1000);
            }
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
            "INFO  com.example.app: Application log 10",
            "INFO  com.example.app: Application log 50",
            "INFO  com.example.app: Application log 51",
            "INFO  com.example.app: Application log 52",
            "INFO  com.example.app: Application log 53",
            "INFO  com.example.app: Application log 54",
            "INFO  com.example.app: Application log 55",
            "INFO  com.example.app: Application log 56",
            "INFO  com.example.app: Application log 57",
            "INFO  com.example.app: Application log 58",
            "INFO  com.example.app: Application log 59"
        );
    }

}
