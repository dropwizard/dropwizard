package io.dropwizard.request.logging;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.server.HttpChannelState;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.Appender;

public class LogbackAccessRequestLogTest {

    @SuppressWarnings("unchecked")
    private final Appender<IAccessEvent> appender = mock(Appender.class);
    private final LogbackAccessRequestLog requestLog = new LogbackAccessRequestLog();

    private final Request request = mock(Request.class);
    private final Response response = mock(Response.class);
    private final HttpChannelState channelState = mock(HttpChannelState.class);

    @Before
    public void setUp() throws Exception {
        when(channelState.isInitial()).thenReturn(true);

        when(request.getRemoteAddr()).thenReturn("10.0.0.1");
        when(request.getTimeStamp()).thenReturn(TimeUnit.SECONDS.toMillis(1353042047));
        when(request.getMethod()).thenReturn("GET");
        when(request.getRequestURI()).thenReturn("/test/things?yay");
        when(request.getProtocol()).thenReturn("HTTP/1.1");
        when(request.getHttpChannelState()).thenReturn(channelState);

        when(response.getStatus()).thenReturn(200);
        when(response.getContentCount()).thenReturn(8290L);

        requestLog.addAppender(appender);

        requestLog.start();
    }

    @After
    public void tearDown() throws Exception {
        requestLog.stop();
    }

    @Test
    public void logsRequestsToTheAppender() {
        final IAccessEvent event = logAndCapture();

        assertThat(event.getRemoteAddr()).isEqualTo("10.0.0.1");
        assertThat(event.getMethod()).isEqualTo("GET");
        assertThat(event.getRequestURI()).isEqualTo("/test/things?yay");
        assertThat(event.getProtocol()).isEqualTo("HTTP/1.1");

        assertThat(event.getStatusCode()).isEqualTo(200);
        assertThat(event.getContentLength()).isEqualTo(8290L);
    }

    private IAccessEvent logAndCapture() {
        requestLog.log(request, response);

        final ArgumentCaptor<IAccessEvent> captor = ArgumentCaptor.forClass(IAccessEvent.class);
        verify(appender, timeout(1000)).doAppend(captor.capture());

        return captor.getValue();
    }
}
