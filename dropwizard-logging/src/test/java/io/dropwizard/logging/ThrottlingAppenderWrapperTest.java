package io.dropwizard.logging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import javax.annotation.Nullable;

import org.apache.commons.text.StringSubstitutor;
import org.assertj.core.api.Condition;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.google.common.util.concurrent.RateLimiter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import io.dropwizard.configuration.ConfigurationValidationException;
import io.dropwizard.configuration.FileConfigurationSourceProvider;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.util.Duration;
import io.dropwizard.validation.BaseValidator;

public class ThrottlingAppenderWrapperTest {

    // Test Constants:
    private static final String LINE_SEPERATOR = System.getProperty("line.separator");
    private static final double NANOS_PER_SECOND = TimeUnit.SECONDS.toNanos(1L);

    // Test defaults:
    private static final int WRITE_LINES = 100;
    private static final Duration RUN_WINDOW = Duration.seconds(1);

    // Test helpers:
    private static final String APP_LOG_PREFIX = "Application log";
    private static final Condition<String> APP_LOG_CONDITION = new Condition<>(o -> o.contains(APP_LOG_PREFIX), "contains application log");

    // Asynchronous timeouts
    private static final Duration LOG_WAIT = Duration.seconds(90);

    @SuppressWarnings("rawtypes")
    private final YamlConfigurationFactory<ConsoleAppenderFactory> factory;
    private final ByteArrayOutputStream bos;

    @Nullable
    private PrintStream oldSysOut;

    @Nullable
    private PrintStream newSysOut;

    public ThrottlingAppenderWrapperTest() {
        super();
        this.oldSysOut = null;
        this.bos = new ByteArrayOutputStream();

        this.factory = new YamlConfigurationFactory<>(
            ConsoleAppenderFactory.class,
            BaseValidator.newValidator(),
            Jackson.newObjectMapper(),
            "dw");
    }

    @Before
    public void setup() throws UnsupportedEncodingException {
        this.oldSysOut = System.out;
        // This forces auto-flush, which should help with buffering issues
        this.newSysOut = new PrintStream(this.bos, true, StandardCharsets.UTF_8.name());
        System.setOut(this.newSysOut);
    }

    @After
    public void teardown() {
        if (this.oldSysOut != null) {
            System.setOut(this.oldSysOut);
        }

        if (this.newSysOut != null) {
            this.newSysOut.close();
        }

        this.bos.reset();
    }

    @Test
    public void appenderWithZeroMessageRate() {
        assertThatThrownBy(() -> this.factory.build(this.findResource("/yaml/appender_with_zero_message_rate.yml")))
            .isInstanceOf(ConfigurationValidationException.class)
            .hasMessageContaining("messageRate must be greater than 0 SECONDS");
    }

    @Test
    public void appenderWithInvalidMessageRate() {
        assertThatThrownBy(() -> this.factory.build(this.findResource("/yaml/appender_with_invalid_message_rate.yml")))
            .isInstanceOf(ConfigurationValidationException.class)
            .hasMessageContaining("messageRate must be less than or equal to 1 MINUTES");
    }

    @Test
    public void overThrottlingLimit() throws Exception {
        this.runLineTest(Duration.milliseconds(100), WRITE_LINES, RUN_WINDOW);
    }

    @Test
    public void belowThrottlingLimit() throws Exception {
        this.runLineTest(Duration.microseconds(1), WRITE_LINES, RUN_WINDOW);
    }

    private void runLineTest(final Duration messageRate, final int lineCount, final Duration runWindow) throws Exception {
        final HashMap<String, Object> variables = new HashMap<>();
        variables.put("messageRate", messageRate);

        @SuppressWarnings("unchecked")
        final ConsoleAppenderFactory<ILoggingEvent> config = this.factory.build(
            new SubstitutingSourceProvider(new FileConfigurationSourceProvider(), new StringSubstitutor(variables)),
            this.findResource("/yaml/logging-message-rate.yml").getPath());

        final DefaultLoggingFactory defaultLoggingFactory = new DefaultLoggingFactory();
        defaultLoggingFactory.setAppenders(Collections.singletonList(config));
        defaultLoggingFactory.configure(new MetricRegistry(), "test-logger");

        final Logger logger = LoggerFactory.getLogger("com.example.app");

        final long start = System.nanoTime();

        final long rwNanos = runWindow.toNanoseconds();
        final double seconds = rwNanos / NANOS_PER_SECOND;

        // We need to subtract 1 as we automatically get one at time zero; how
        // long should the remaining take?
        final double limit = (lineCount - 1) / seconds;

        final RateLimiter rateLimiter = RateLimiter.create(limit);
        for (int i = 0; i < lineCount; i++) {
            rateLimiter.acquire();
            logger.info("{} {}", APP_LOG_PREFIX, i);
        }

        final long elapsed = System.nanoTime() - start;

        // We need to sleep for at least message duration to ensure our line
        // will be written and thus invoke the countdown on the latch.
        Thread.sleep(messageRate.toMilliseconds() + 1L);

        final LogWait lwait = new LogWait(1);
        logger.info("Log termination {}", lwait);
        assertThat(lwait.block(LOG_WAIT.toNanoseconds(), TimeUnit.NANOSECONDS)).isTrue();

        // Force logger to flush
        defaultLoggingFactory.stop();

        // Force streams to flush. NullAway doesn't understand that if we made
        // it here, these can't be null. C'est la vie.
        if (this.newSysOut != null) {
            this.newSysOut.flush();
        }

        final byte[] rawBuffer = this.bos.toByteArray();

        final String strBuffer = new String(rawBuffer, StandardCharsets.UTF_8);
        final String[] logArray = strBuffer.split(LINE_SEPERATOR);

        final long mrNanos = messageRate.toNanoseconds();

        // Note that when we write slower than our threshold, more intervals
        // will elapse than we have lines, thus we need a Math.min guard rail so
        // our tests function.
        final double messagesPerWindow = (double) rwNanos / (double) mrNanos;

        // We add 1 as we get an automatic message at time zero
        final int low = Math.min((int) messagesPerWindow, lineCount);

        // How many durations did we cover (i.e how many log lines do we expect
        // worst case host performance)
        final double intervals = (double) elapsed / (double) mrNanos;

        // If we had a fractional interval, round up instead of down. We have to
        // add one regardless as at full speed, we end up with 1 + executed
        // intervals as an event fires at time zero.
        int high = 1 + (int) Math.ceil(intervals);
        high = Math.min(high, lineCount);

        assertThat(Arrays.asList(logArray))
            .doesNotHaveDuplicates()
            .haveAtLeast(low, APP_LOG_CONDITION)
            .haveAtMost(high, APP_LOG_CONDITION);
    }

    private File findResource(final String resourceName) throws URISyntaxException {
        return new File(this.getClass().getResource(resourceName).toURI());
    }

    /**
     * Helper class that unblocks when it is turned into a log string.
     */
    private static final class LogWait {

        private final CountDownLatch latch;

        LogWait(final int latchCount) {
            super();
            this.latch = new CountDownLatch(latchCount);
        }

        public boolean block(final long waitTime, final TimeUnit waitUnit) throws InterruptedException {
            return this.latch.await(waitTime, waitUnit);
        }

        @Override
        public String toString() {
            this.latch.countDown();
            return super.toString();
        }
    }
}
