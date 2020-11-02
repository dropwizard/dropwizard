package io.dropwizard.logging;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.core.pattern.PatternLayoutBase;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.logging.async.AsyncLoggingEventAppenderFactory;
import io.dropwizard.logging.filter.NullLevelFilterFactory;
import io.dropwizard.logging.layout.DropwizardLayoutFactory;
import io.dropwizard.util.Resources;
import io.dropwizard.validation.BaseValidator;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.net.URISyntaxException;
import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

public class AppenderFactoryCustomTimeZone {

    static {
        BootstrapLogging.bootstrap();
    }

    @SuppressWarnings("rawtypes")
    private final YamlConfigurationFactory<ConsoleAppenderFactory> factory = new YamlConfigurationFactory<>(
        ConsoleAppenderFactory.class, BaseValidator.newValidator(), Jackson.newObjectMapper(), "dw");

    private static File loadResource(String resourceName) throws URISyntaxException {
        return new File(Resources.getResource(resourceName).toURI());
    }

    @Test
    public void testLoadAppenderWithTimeZoneInFullFormat() throws Exception {
        final ConsoleAppenderFactory<?> appender = factory.build(loadResource("yaml/appender_with_time_zone_in_full_format.yml"));
        assertThat(appender.getTimeZone().getID()).isEqualTo("America/Los_Angeles");
    }

    @Test
    public void testLoadAppenderWithTimeZoneInCustomFormat() throws Exception {
        final ConsoleAppenderFactory<?> appender = factory.build(loadResource("yaml/appender_with_custom_time_zone_format.yml"));
        assertThat(appender.getTimeZone().getID()).isEqualTo("GMT-02:00");
    }

    @Test
    public void testLoadAppenderWithNoTimeZone() throws Exception {
        final ConsoleAppenderFactory<?> appender = factory.build(loadResource("yaml/appender_with_no_time_zone.yml"));
        assertThat(appender.getTimeZone().getID()).isEqualTo("UTC");
    }

    @Test
    public void testLoadAppenderWithUtcTimeZone() throws Exception {
        final ConsoleAppenderFactory<?> appender = factory.build(loadResource("yaml/appender_with_utc_time_zone.yml"));
        assertThat(appender.getTimeZone().getID()).isEqualTo("UTC");
    }

    @Test
    public void testLoadAppenderWithWrongTimeZone() throws Exception {
        final ConsoleAppenderFactory<?> appender = factory.build(loadResource("yaml/appender_with_wrong_time_zone.yml"));
        assertThat(appender.getTimeZone().getID()).isEqualTo("GMT");
    }

    @Test
    public void testLoadAppenderWithSystemTimeZone() throws Exception {
        final ConsoleAppenderFactory<?> appender = factory.build(loadResource("yaml/appender_with_system_time_zone.yml"));
        assertThat(appender.getTimeZone()).isEqualTo(TimeZone.getDefault());
    }

    @Test
    public void testBuildAppenderWithTimeZonePlaceholderInLogFormat() throws Exception {
        ConsoleAppender<?> consoleAppender = buildAppender("yaml/appender_with_time_zone_placeholder.yml");
        LayoutWrappingEncoder<?> encoder = (LayoutWrappingEncoder<?>) consoleAppender.getEncoder();
        PatternLayoutBase<?> layout = (PatternLayoutBase<?>) encoder.getLayout();
        assertThat(layout.getPattern()).isEqualTo("custom format with UTC");
    }

    @SuppressWarnings("unchecked")
    private ConsoleAppender<?> buildAppender(String resourceName) throws Exception {
        AsyncAppender appender = (AsyncAppender) factory.build(loadResource(resourceName))
            .build(new LoggerContext(), "test-custom-time-zone", new DropwizardLayoutFactory(),
                new NullLevelFilterFactory<>(), new AsyncLoggingEventAppenderFactory());
        return (ConsoleAppender<?>) appender.getAppender("console-appender");
    }

}
