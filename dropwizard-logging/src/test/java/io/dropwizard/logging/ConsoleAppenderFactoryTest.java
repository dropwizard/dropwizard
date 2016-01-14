package io.dropwizard.logging;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Layout;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.logging.filter.NullFilterFactory;
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
        final LoggerContext context = new LoggerContext();
        final Layout<ILoggingEvent> layout = new DropwizardLayout(context, "%-5p [%d{ISO8601,UTC}] %c: %m%n%rEx");
        layout.start();
        ConsoleAppenderFactory<ILoggingEvent> consoleAppenderFactory = new ConsoleAppenderFactory<>();
        AsyncAppender asyncAppender = (AsyncAppender) consoleAppenderFactory.build(context, "test", layout, new NullFilterFactory<ILoggingEvent>(), new AsyncLoggingEventAppenderFactory());
        assertThat(asyncAppender.isIncludeCallerData()).isFalse();

        consoleAppenderFactory.setIncludeCallerData(true);
        asyncAppender = (AsyncAppender) consoleAppenderFactory.build(context, "test", layout, new NullFilterFactory<ILoggingEvent>(), new AsyncLoggingEventAppenderFactory());
        assertThat(asyncAppender.isIncludeCallerData()).isTrue();
    }

    @Test
    public void appenderContextIsSet() throws Exception {
        final Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        final LoggerContext context = root.getLoggerContext();
        final Layout<ILoggingEvent> layout = new DropwizardLayout(context, "%-5p [%d{ISO8601,UTC}] %c: %m%n%rEx");
        layout.start();

        final ConsoleAppenderFactory<ILoggingEvent> appenderFactory = new ConsoleAppenderFactory<>();
        final Appender<ILoggingEvent> appender = appenderFactory.build(context, "test", layout, new NullFilterFactory<ILoggingEvent>(), new AsyncLoggingEventAppenderFactory());

        assertThat(appender.getContext()).isEqualTo(root.getLoggerContext());
    }

    @Test
    public void appenderNameIsSet() throws Exception {
        final Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        final LoggerContext context = root.getLoggerContext();
        final Layout<ILoggingEvent> layout = new DropwizardLayout(context, "%-5p [%d{ISO8601,UTC}] %c: %m%n%rEx");
        layout.start();

        final ConsoleAppenderFactory<ILoggingEvent> appenderFactory = new ConsoleAppenderFactory<>();
        final Appender<ILoggingEvent> appender = appenderFactory.build(context, "test", layout, new NullFilterFactory<ILoggingEvent>(), new AsyncLoggingEventAppenderFactory());

        assertThat(appender.getName()).isEqualTo("async-console-appender");
    }
}
