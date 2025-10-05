package io.dropwizard.request.logging.old;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.spi.AppenderAttachableImpl;
import io.dropwizard.logging.common.BootstrapLogging;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.assertArg;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

class DropwizardSlf4jRequestLogWriterTest {
    static {
        BootstrapLogging.bootstrap();
    }

    private final Appender<ILoggingEvent> appender = mock();
    private final AppenderAttachableImpl<ILoggingEvent> appenders = new AppenderAttachableImpl<>();
    private final DropwizardSlf4jRequestLogWriter slf4jRequestLog = new DropwizardSlf4jRequestLogWriter(appenders);

    @BeforeEach
    void setUp() throws Exception {
        appenders.addAppender(appender);

        slf4jRequestLog.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        slf4jRequestLog.stop();
    }

    @Test
    void logsRequestsToTheAppenders() throws Exception {
        slf4jRequestLog.write("1, 2 buckle my shoe");

        verify(appender, timeout(1000))
            .doAppend(assertArg(loggingEvent -> assertThat(loggingEvent)
                .satisfies(event -> assertThat(event.getFormattedMessage()).isEqualTo("1, 2 buckle my shoe"))
                .satisfies(event -> assertThat(event.getLevel()).isEqualTo(Level.INFO))
                .satisfies(event -> assertThat(event).hasToString("[INFO] 1, 2 buckle my shoe"))));
    }
}
