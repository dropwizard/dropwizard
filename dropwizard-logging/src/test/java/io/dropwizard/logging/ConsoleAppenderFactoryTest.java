package io.dropwizard.logging;

import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import static org.assertj.core.api.Assertions.assertThat;

public class ConsoleAppenderFactoryTest {
    @Test
    public void isDiscoverable() throws Exception {
        assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes())
                .contains(ConsoleAppenderFactory.class);
    }

    @Test
    public void appenderContextIsSet() throws Exception {
        final Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        final ConsoleAppenderFactory appenderFactory = new ConsoleAppenderFactory();
        final Appender<ILoggingEvent> appender = appenderFactory.build(root.getLoggerContext(), "test", null);

        assertThat(appender.getContext()).isEqualTo(root.getLoggerContext());
    }

    @Test
    public void appenderNameIsSet() throws Exception {
        final Logger root = (Logger) LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME);
        final ConsoleAppenderFactory appenderFactory = new ConsoleAppenderFactory();
        final Appender<ILoggingEvent> appender = appenderFactory.build(root.getLoggerContext(), "test", null);

        assertThat(appender.getName()).isEqualTo("async-console-appender");
    }
}
