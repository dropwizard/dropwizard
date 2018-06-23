package io.dropwizard.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import com.codahale.metrics.MetricRegistry;
import com.google.common.io.Resources;
import com.google.common.util.concurrent.RateLimiter;
import io.dropwizard.configuration.ConfigurationValidationException;
import io.dropwizard.configuration.FileConfigurationSourceProvider;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.util.Duration;
import io.dropwizard.validation.BaseValidator;
import org.apache.commons.text.StrSubstitutor;
import org.assertj.core.api.Condition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ThrottlingAppenderTest {
    private final YamlConfigurationFactory<ConsoleAppenderFactory> factory = new YamlConfigurationFactory<>(
        ConsoleAppenderFactory.class,
        BaseValidator.newValidator(),
        Jackson.newObjectMapper(),
        "dw");

    private PrintStream old;
    private final Condition<String> containsApplicationLog =
        new Condition<>(o -> o.contains("Application log"), "contains application log");

    private final ByteArrayOutputStream redirectedStream = new ByteArrayOutputStream();

    @Before
    public void setup() {
        old = System.out;
        System.setOut(new PrintStream(redirectedStream));
    }

    @After
    public void teardown() {
        System.setOut(old);
    }

    private static File loadResource(String resourceName) throws URISyntaxException {
        return new File(Resources.getResource(resourceName).toURI());
    }

    @SuppressWarnings("unchecked")
    private List<String> throttledLines(Duration messageRate) throws Exception {
        final HashMap<String, Object> variables = new HashMap<>();
        variables.put("messageRate", messageRate);
        final ConsoleAppenderFactory<ILoggingEvent> config = factory.build(
            new SubstitutingSourceProvider(new FileConfigurationSourceProvider(), new StrSubstitutor(variables)),
            loadResource("yaml/logging-message-rate.yml").getPath());
        final DefaultLoggingFactory defaultLoggingFactory = new DefaultLoggingFactory();
        defaultLoggingFactory.setAppenders(Collections.singletonList(config));
        defaultLoggingFactory.configure(new MetricRegistry(), "test-logger");

        final Logger logger = LoggerFactory.getLogger("com.example.app");

        final OffsetDateTime start = OffsetDateTime.now();

        // Creating a ratelimiter that allows 100 acquires a second will
        // request 100 lines to log over approximately a second
        final RateLimiter rateLimiter = RateLimiter.create(100);
        for (int i = 0; i < 100; i++) {
            rateLimiter.acquire();
            logger.info("Application log {}", i);
        }

        // But just as sanity check we ensure that approximately a second has elapsed
        final OffsetDateTime end = OffsetDateTime.now();
        assertThat(ChronoUnit.MILLIS.between(start, end)).isBetween(900L, 1500L);

        final String logs = new String(redirectedStream.toByteArray(), UTF_8);
        return Arrays.asList(logs.split("\\r?\\n"));
    }

    @Test
    public void appenderWithZeroMessageRate() {
        assertThatThrownBy(() -> factory.build(loadResource("yaml/appender_with_zero_message_rate.yml")))
            .isInstanceOf(ConfigurationValidationException.class)
            .hasMessageContaining("messageRate must be greater than (or equal to, if in 'inclusive' mode) 0 SECONDS");
    }

    @Test
    public void appenderWithInvalidMessageRate() {
        assertThatThrownBy(() -> factory.build(loadResource("yaml/appender_with_invalid_message_rate.yml")))
            .isInstanceOf(ConfigurationValidationException.class)
            .hasMessageContaining("messageRate must be less than (or equal to, if in 'inclusive' mode) 1 MINUTES");
    }

    @Test
    public void overThrottlingLimit() throws Exception {
        // Allowing a message every 100ms will cause approximately 10 messages to
        // be logged (fluctuations in how CI is feeling is accounted for)
        assertThat(throttledLines(Duration.milliseconds(100)))
            .doesNotHaveDuplicates()
            .haveAtLeast(9, containsApplicationLog)
            .haveAtMost(12, containsApplicationLog);
    }

    @Test
    public void belowThrottlingLimit() throws Exception {
        // Allowing a message every 1ms will most likely cause all 100 messages to be logged
        assertThat(throttledLines(Duration.milliseconds(1)))
            .doesNotHaveDuplicates()
            .haveAtLeast(98, containsApplicationLog)
            .haveAtMost(100, containsApplicationLog);
    }
}
