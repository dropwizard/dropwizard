package io.dropwizard.logging.common;

import static org.assertj.core.api.Assertions.assertThat;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.net.SyslogAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.logging.common.async.AsyncLoggingEventAppenderFactory;
import io.dropwizard.logging.common.filter.NullLevelFilterFactory;
import io.dropwizard.logging.common.layout.DropwizardLayoutFactory;
import java.util.Locale;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

class SyslogAppenderFactoryTest {

    static {
        BootstrapLogging.bootstrap();
    }

    @Test
    void isDiscoverable() {
        assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes()).contains(SyslogAppenderFactory.class);
    }

    @Test
    void defaultIncludesAppName() {
        assertThat(new SyslogAppenderFactory().getLogFormat()).contains("%app");
    }

    @Test
    void defaultIncludesPid() {
        assertThat(new SyslogAppenderFactory().getLogFormat()).contains("%pid");
    }

    @Test
    void patternIncludesAppNameAndPid() {
        assertThat(new SyslogAppenderFactory()
                        .build(
                                new LoggerContext(),
                                "MyApplication",
                                new DropwizardLayoutFactory(),
                                new NullLevelFilterFactory<>(),
                                new AsyncLoggingEventAppenderFactory()))
                .isInstanceOfSatisfying(
                        AsyncAppender.class, asyncAppender -> assertThat(asyncAppender.getAppender("syslog-appender"))
                                .isInstanceOfSatisfying(SyslogAppender.class, syslogAppender -> assertThat(
                                                syslogAppender.getSuffixPattern())
                                        .matches("^MyApplication\\[\\d+\\].+")));
    }

    @Test
    void stackTracePatternCanBeSet() {
        final SyslogAppenderFactory syslogAppenderFactory = new SyslogAppenderFactory();
        syslogAppenderFactory.setStackTracePrefix("--->");

        assertThat(syslogAppenderFactory.build(
                        new LoggerContext(),
                        "MyApplication",
                        new DropwizardLayoutFactory(),
                        new NullLevelFilterFactory<>(),
                        new AsyncLoggingEventAppenderFactory()))
                .isInstanceOfSatisfying(
                        AsyncAppender.class, asyncAppender -> assertThat(asyncAppender.getAppender("syslog-appender"))
                                .isInstanceOfSatisfying(SyslogAppender.class, syslogAppender -> assertThat(
                                                syslogAppender.getStackTracePattern())
                                        .isEqualTo("--->")));
    }

    @Test
    void appenderContextIsSet() {
        final Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        final SyslogAppenderFactory appenderFactory = new SyslogAppenderFactory();
        final Appender<ILoggingEvent> appender = appenderFactory.build(
                root.getLoggerContext(),
                "test",
                new DropwizardLayoutFactory(),
                new NullLevelFilterFactory<>(),
                new AsyncLoggingEventAppenderFactory());

        assertThat(appender.getContext()).isEqualTo(root.getLoggerContext());
    }

    @Test
    void appenderNameIsSet() {
        final Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        final SyslogAppenderFactory appenderFactory = new SyslogAppenderFactory();
        final Appender<ILoggingEvent> appender = appenderFactory.build(
                root.getLoggerContext(),
                "test",
                new DropwizardLayoutFactory(),
                new NullLevelFilterFactory<>(),
                new AsyncLoggingEventAppenderFactory());

        assertThat(appender.getName()).isEqualTo("async-syslog-appender");
    }

    @Test
    void syslogFacilityTest() {
        for (SyslogAppenderFactory.Facility facility : SyslogAppenderFactory.Facility.values()) {
            assertThat(SyslogAppender.facilityStringToint(facility.toString().toLowerCase(Locale.ENGLISH)))
                    .isNotNegative();
        }
    }
}
