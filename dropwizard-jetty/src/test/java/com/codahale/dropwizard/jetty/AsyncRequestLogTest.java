package com.codahale.dropwizard.jetty;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.spi.AppenderAttachableImpl;
import com.codahale.dropwizard.util.Duration;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.server.HttpChannelState;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class AsyncRequestLogTest {
    @SuppressWarnings("unchecked")
    private final Appender<ILoggingEvent> appender = mock(Appender.class);
    private final AppenderAttachableImpl<ILoggingEvent> appenders = new AppenderAttachableImpl<>();
    private final AsyncRequestLog asyncRequestLog = new AsyncRequestLog(appenders,
                                                                        TimeZone.getTimeZone("UTC"),
                                                                        1_000,
                                                                        Duration.milliseconds(100));

    private final Request request = mock(Request.class);
    private final Response response = mock(Response.class);
    private final HttpChannelState channelState = mock(HttpChannelState.class);

    @Before
    public void setUp() throws Exception {
        when(channelState.isInitial()).thenReturn(true);

        when(request.getRemoteAddr()).thenReturn("10.0.0.1");
        when(request.getTimeStamp()).thenReturn(TimeUnit.SECONDS.toMillis(1353042047));
        when(request.getMethod()).thenReturn("GET");
        when(request.getUri()).thenReturn(new HttpURI("/test/things?yay"));
        when(request.getProtocol()).thenReturn("HTTP/1.1");
        when(request.getHttpChannelState()).thenReturn(channelState);
        when(request.getDispatchTime()).thenReturn(TimeUnit.SECONDS.toMillis(1353042048));

        when(response.getStatus()).thenReturn(200);
        when(response.getContentCount()).thenReturn(8290L);

        appenders.addAppender(appender);

        asyncRequestLog.start();
    }

    @After
    public void tearDown() throws Exception {
        if (asyncRequestLog.isRunning()) {
            asyncRequestLog.stop();
        }
    }

    @Test
    public void startsAndStops() throws Exception {
        asyncRequestLog.stop();

        verify(appender, timeout(1000)).stop();
    }

    @Test
    public void logsRequestsToTheAppenders() throws Exception {
        final ILoggingEvent event = logAndCapture();

        // It would be lovely if the clock could be injected so we could test this reliably, but
        // I suppose we should just trust the Jetty folks.
        assertThat(event.getFormattedMessage())
                .startsWith("10.0.0.1");

        assertThat(event.getLevel())
                .isEqualTo(Level.INFO);
    }

    private ILoggingEvent logAndCapture() {
        asyncRequestLog.log(request, response);

        final ArgumentCaptor<ILoggingEvent> captor = ArgumentCaptor.forClass(ILoggingEvent.class);
        verify(appender, timeout(1000)).doAppend(captor.capture());

        return captor.getValue();
    }
}
