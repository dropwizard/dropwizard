package io.dropwizard.request.logging.old;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.spi.AppenderAttachableImpl;
import io.dropwizard.logging.BootstrapLogging;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.server.HttpChannelState;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DropwizardSlf4jRequestLogWriterTest {
    static {
        BootstrapLogging.bootstrap();
    }

    @SuppressWarnings("unchecked")
    private final Appender<ILoggingEvent> appender = mock(Appender.class);
    private final AppenderAttachableImpl<ILoggingEvent> appenders = new AppenderAttachableImpl<>();
    private final DropwizardSlf4jRequestLogWriter slf4jRequestLog = new DropwizardSlf4jRequestLogWriter(appenders);

    private final Request request = mock(Request.class);
    private final Response response = mock(Response.class, RETURNS_DEEP_STUBS);
    private final HttpChannelState channelState = mock(HttpChannelState.class);

    @BeforeEach
    public void setUp() throws Exception {
        when(channelState.isInitial()).thenReturn(true);

        when(request.getRemoteHost()).thenReturn("10.0.0.1");
        when(request.getTimeStamp()).thenReturn(TimeUnit.SECONDS.toMillis(1353042047));
        when(request.getMethod()).thenReturn("GET");
        when(request.getHttpURI()).thenReturn(new HttpURI("/test/things?yay"));
        when(request.getProtocol()).thenReturn("HTTP/1.1");
        when(request.getHttpChannelState()).thenReturn(channelState);
        when(request.getTimeStamp()).thenReturn(TimeUnit.SECONDS.toMillis(1353042048));

        when(response.getCommittedMetaData().getStatus()).thenReturn(200);
        when(response.getHttpChannel().getBytesWritten()).thenReturn(8290L);

        appenders.addAppender(appender);

        slf4jRequestLog.start();
    }

    @AfterEach
    public void tearDown() throws Exception {
        slf4jRequestLog.stop();
    }

    @Test
    public void logsRequestsToTheAppenders() throws Exception {
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
