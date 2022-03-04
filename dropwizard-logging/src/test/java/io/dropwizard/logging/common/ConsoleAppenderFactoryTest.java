package io.dropwizard.logging.common;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.logging.common.BootstrapLogging;
import io.dropwizard.logging.common.ConsoleAppenderFactory;
import io.dropwizard.logging.common.async.AsyncLoggingEventAppenderFactory;
import io.dropwizard.logging.common.filter.NullLevelFilterFactory;
import io.dropwizard.logging.common.layout.DropwizardLayoutFactory;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

class ConsoleAppenderFactoryTest {
    static {
        BootstrapLogging.bootstrap();
    }

    @Test
    void isDiscoverable() {
        assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes())
                .contains(ConsoleAppenderFactory.class);
    }

    @Test
    void includesCallerData() {
        ConsoleAppenderFactory<ILoggingEvent> consoleAppenderFactory = new ConsoleAppenderFactory<>();
        assertThat(consoleAppenderFactory.build(new LoggerContext(), "test", new DropwizardLayoutFactory(), new NullLevelFilterFactory<>(), new AsyncLoggingEventAppenderFactory()))
            .isInstanceOfSatisfying(AsyncAppender.class, asyncAppender -> assertThat(asyncAppender.isIncludeCallerData()).isFalse());

        consoleAppenderFactory.setIncludeCallerData(true);

        assertThat(consoleAppenderFactory.build(new LoggerContext(), "test", new DropwizardLayoutFactory(), new NullLevelFilterFactory<>(), new AsyncLoggingEventAppenderFactory()))
            .isInstanceOfSatisfying(AsyncAppender.class, asyncAppender -> assertThat(asyncAppender.isIncludeCallerData()).isTrue());
    }

    @Test
    void appenderContextIsSet() {
        final Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        final ConsoleAppenderFactory<ILoggingEvent> appenderFactory = new ConsoleAppenderFactory<>();
        final Appender<ILoggingEvent> appender = appenderFactory.build(root.getLoggerContext(), "test", new DropwizardLayoutFactory(), new NullLevelFilterFactory<>(), new AsyncLoggingEventAppenderFactory());

        assertThat(appender.getContext()).isEqualTo(root.getLoggerContext());
    }

    @Test
    void appenderNameIsSet() {
        final Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        final ConsoleAppenderFactory<ILoggingEvent> appenderFactory = new ConsoleAppenderFactory<>();
        final Appender<ILoggingEvent> appender = appenderFactory.build(root.getLoggerContext(), "test", new DropwizardLayoutFactory(), new NullLevelFilterFactory<>(), new AsyncLoggingEventAppenderFactory());

        assertThat(appender.getName()).isEqualTo("async-console-appender");
    }

    @Test
    void isNeverBlock() {
        ConsoleAppenderFactory<ILoggingEvent> consoleAppenderFactory = new ConsoleAppenderFactory<>();
        consoleAppenderFactory.setNeverBlock(true);
        assertThat(consoleAppenderFactory.build(new LoggerContext(), "test", new DropwizardLayoutFactory(), new NullLevelFilterFactory<>(), new AsyncLoggingEventAppenderFactory()))
            .isInstanceOfSatisfying(AsyncAppender.class, asyncAppender -> assertThat(asyncAppender.isNeverBlock()).isTrue());
    }

    @Test
    void isNotNeverBlock() {
        ConsoleAppenderFactory<ILoggingEvent> consoleAppenderFactory = new ConsoleAppenderFactory<>();
        consoleAppenderFactory.setNeverBlock(false);
        assertThat(consoleAppenderFactory.build(new LoggerContext(), "test", new DropwizardLayoutFactory(), new NullLevelFilterFactory<>(), new AsyncLoggingEventAppenderFactory()))
            .isInstanceOfSatisfying(AsyncAppender.class, asyncAppender -> assertThat(asyncAppender.isNeverBlock()).isFalse());
    }

    @Test
    void defaultIsNotNeverBlock() {
        ConsoleAppenderFactory<ILoggingEvent> consoleAppenderFactory = new ConsoleAppenderFactory<>();
        // default neverBlock
        assertThat(consoleAppenderFactory.build(new LoggerContext(), "test", new DropwizardLayoutFactory(), new NullLevelFilterFactory<>(), new AsyncLoggingEventAppenderFactory()))
            .isInstanceOfSatisfying(AsyncAppender.class, asyncAppender -> assertThat(asyncAppender.isNeverBlock()).isFalse());
    }

}
