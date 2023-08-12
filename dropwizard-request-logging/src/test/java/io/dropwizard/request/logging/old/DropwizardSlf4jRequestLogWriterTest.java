package io.dropwizard.request.logging.old;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.spi.AppenderAttachableImpl;
import io.dropwizard.logging.common.BootstrapLogging;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;

class DropwizardSlf4jRequestLogWriterTest {
    static {
        BootstrapLogging.bootstrap();
    }

    @SuppressWarnings("unchecked")
    private final Appender<ILoggingEvent> appender = mock(Appender.class);
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
        final String requestLine = "1, 2 buckle my shoe";
        slf4jRequestLog.write(requestLine);
        final ArgumentCaptor<ILoggingEvent> captor = ArgumentCaptor.forClass(ILoggingEvent.class);
        verify(appender, timeout(1000)).doAppend(captor.capture());

        final ILoggingEvent event = captor.getValue();
        assertThat(event.getFormattedMessage()).isEqualTo(requestLine);
        assertThat(event.getLevel()).isEqualTo(Level.INFO);
        assertThat(event).hasToString("[INFO] 1, 2 buckle my shoe");
    }
}
