package io.dropwizard.logging;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.logging.async.AsyncLoggingEventAppenderFactory;
import io.dropwizard.logging.filter.NullLevelFilterFactory;
import io.dropwizard.logging.layout.DropwizardLayoutFactory;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class ConsoleAppenderFactoryTest {
    static {
        BootstrapLogging.bootstrap();
    }

    @Test
    public void isDiscoverable() throws Exception {
        assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes())
                .contains(ConsoleAppenderFactory.class);
    }

    @Test
    public void includesCallerData() {
        ConsoleAppenderFactory<ILoggingEvent> consoleAppenderFactory = new ConsoleAppenderFactory<>();
        AsyncAppender asyncAppender = (AsyncAppender) consoleAppenderFactory.build(new LoggerContext(), "test", new DropwizardLayoutFactory(), new NullLevelFilterFactory<>(), new AsyncLoggingEventAppenderFactory());
        assertThat(asyncAppender.isIncludeCallerData()).isFalse();

        consoleAppenderFactory.setIncludeCallerData(true);
        asyncAppender = (AsyncAppender) consoleAppenderFactory.build(new LoggerContext(), "test", new DropwizardLayoutFactory(), new NullLevelFilterFactory<>(), new AsyncLoggingEventAppenderFactory());
        assertThat(asyncAppender.isIncludeCallerData()).isTrue();
    }

    @Test
    public void appenderContextIsSet() throws Exception {
        final Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        final ConsoleAppenderFactory<ILoggingEvent> appenderFactory = new ConsoleAppenderFactory<>();
        final Appender<ILoggingEvent> appender = appenderFactory.build(root.getLoggerContext(), "test", new DropwizardLayoutFactory(), new NullLevelFilterFactory<>(), new AsyncLoggingEventAppenderFactory());

        assertThat(appender.getContext()).isEqualTo(root.getLoggerContext());
    }

    @Test
    public void appenderNameIsSet() throws Exception {
        final Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        final ConsoleAppenderFactory<ILoggingEvent> appenderFactory = new ConsoleAppenderFactory<>();
        final Appender<ILoggingEvent> appender = appenderFactory.build(root.getLoggerContext(), "test", new DropwizardLayoutFactory(), new NullLevelFilterFactory<>(), new AsyncLoggingEventAppenderFactory());

        assertThat(appender.getName()).isEqualTo("async-console-appender");
    }
}
