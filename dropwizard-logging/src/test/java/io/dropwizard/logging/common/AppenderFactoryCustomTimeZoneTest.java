package io.dropwizard.logging.common;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.encoder.LayoutWrappingEncoder;
import ch.qos.logback.core.pattern.PatternLayoutBase;
import io.dropwizard.configuration.ConfigurationSourceProvider;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.logging.common.async.AsyncLoggingEventAppenderFactory;
import io.dropwizard.logging.common.filter.NullLevelFilterFactory;
import io.dropwizard.logging.common.layout.DropwizardLayoutFactory;
import io.dropwizard.validation.BaseValidator;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.TimeZone;

import static org.assertj.core.api.Assertions.assertThat;

class AppenderFactoryCustomTimeZoneTest {
    static {
        BootstrapLogging.bootstrap();
    }

    private final ConfigurationSourceProvider configurationSourceProvider = new ResourceConfigurationSourceProvider();

    @SuppressWarnings("rawtypes")
    private final YamlConfigurationFactory<ConsoleAppenderFactory> factory = new YamlConfigurationFactory<>(
        ConsoleAppenderFactory.class, BaseValidator.newValidator(), Jackson.newObjectMapper(), "dw");

    @ParameterizedTest
    @CsvSource({
        "yaml/appender_with_time_zone_in_full_format.yml, America/Los_Angeles",
        "yaml/appender_with_custom_time_zone_format.yml, GMT-02:00",
        "yaml/appender_with_no_time_zone.yml, UTC",
        "yaml/appender_with_utc_time_zone.yml, UTC",
        "yaml/appender_with_wrong_time_zone.yml, GMT"
    })
    void testAppenderTimeZone(String configurationFile, String timeZone) throws Exception {
        final ConsoleAppenderFactory<?> appender = factory.build(configurationSourceProvider, configurationFile);
        assertThat(appender.getTimeZone().getID()).isEqualTo(timeZone);
    }

    @Test
    void testLoadAppenderWithSystemTimeZone() throws Exception {
        final ConsoleAppenderFactory<?> appender = factory.build(configurationSourceProvider, "yaml/appender_with_system_time_zone.yml");
        assertThat(appender.getTimeZone()).isEqualTo(TimeZone.getDefault());
    }

    @Test
    @SuppressWarnings({"unchecked"})
    void testBuildAppenderWithTimeZonePlaceholderInLogFormat() throws Exception {
        Appender<ILoggingEvent> appender = factory.build(configurationSourceProvider, "yaml/appender_with_time_zone_placeholder.yml")
            .build(new LoggerContext(), "test-custom-time-zone", new DropwizardLayoutFactory(),
                new NullLevelFilterFactory<>(), new AsyncLoggingEventAppenderFactory());

        assertThat(appender)
            .isInstanceOfSatisfying(AsyncAppender.class, asyncAppender -> assertThat(asyncAppender.getAppender("console-appender"))
                .isInstanceOfSatisfying(ConsoleAppender.class, consoleAppender -> assertThat(consoleAppender.getEncoder())
                    .isInstanceOfSatisfying(LayoutWrappingEncoder.class, encoder -> assertThat(encoder.getLayout())
                        .isInstanceOfSatisfying(PatternLayoutBase.class, layout -> assertThat(layout.getPattern())
                            .isEqualTo("custom format with UTC")))));
    }
}
