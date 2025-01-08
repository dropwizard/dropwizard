package io.dropwizard.request.logging;

import ch.qos.logback.access.common.spi.IAccessEvent;
import ch.qos.logback.core.Appender;
import org.eclipse.jetty.ee10.servlet.ServletChannel;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletContextRequest;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.server.ConnectionMetaData;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LogbackAccessRequestLogTest {

    @SuppressWarnings("unchecked")
    private final Appender<IAccessEvent> appender = mock(Appender.class);
    private final LogbackAccessRequestLog requestLog = new LogbackAccessRequestLog();

    private final ServletContextHandler servletContextHandler = new ServletContextHandler();
    private final Request request = mock(Request.class);
    private MockedStatic<Request> staticRequest;
    private final Response response = mock(Response.class);
    private MockedStatic<Response> staticResponse;
    private ServletContextRequest servletContextRequest;

    @BeforeEach
    void setUp() throws Exception {
        ConnectionMetaData connectionMetaData = mock(ConnectionMetaData.class);
        when(response.getHeaders()).thenReturn(HttpFields.build());
        when(connectionMetaData.getHttpConfiguration()).thenReturn(new HttpConfiguration());
        when(request.getConnectionMetaData()).thenReturn(connectionMetaData);

        when(request.getMethod()).thenReturn("GET");
        HttpURI httpURI = mock(HttpURI.class);
        when(httpURI.getPath()).thenReturn("/test/things?yay");
        when(request.getHttpURI()).thenReturn(httpURI);
        when(connectionMetaData.getProtocol()).thenReturn("HTTP/1.1");
        when(request.getConnectionMetaData()).thenReturn(connectionMetaData);
        when(request.getHeaders()).thenReturn(HttpFields.build());

        ServletChannel servletChannel = new ServletChannel(servletContextHandler, connectionMetaData);
        servletContextRequest = new TestServletContextRequest(servletChannel);
        servletChannel.associate(servletContextRequest);

        staticRequest = mockStatic(Request.class);
        staticResponse = mockStatic(Response.class);

        staticRequest.when(() -> Request.getRemoteAddr(servletContextRequest)).thenReturn("10.0.0.1");
        staticRequest.when(() -> Request.getTimeStamp(request)).thenReturn(TimeUnit.SECONDS.toMillis(1353042047));
        staticRequest.when(() -> Request.as(any(), any())).thenCallRealMethod();

        staticResponse.when(() -> Response.getContentBytesWritten(response)).thenReturn(8290L);
        when(response.getStatus()).thenReturn(200);

        HttpFields.Mutable responseFields = HttpFields.build();
        responseFields.add("Testheader", "Testvalue1");
        responseFields.add("Testheader", "Testvalue2");
        when(response.getHeaders()).thenReturn(responseFields);

        requestLog.addAppender(appender);

        requestLog.start();
    }

    @AfterEach
    void tearDown() throws Exception {
        requestLog.stop();
        staticRequest.close();
        staticResponse.close();
    }

    @Test
    void logsRequestsToTheAppender() {
        final IAccessEvent event = logAndCapture();

        assertThat(event.getRemoteAddr()).isEqualTo("10.0.0.1");
        assertThat(event.getMethod()).isEqualTo("GET");
        assertThat(event.getRequestURI()).isEqualTo("/test/things?yay");
        assertThat(event.getProtocol()).isEqualTo("HTTP/1.1");

        assertThat(event.getStatusCode()).isEqualTo(200);
        assertThat(event.getContentLength()).isEqualTo(8290L);
    }

    @Test
    void combinesHeaders() {
        final IAccessEvent event = logAndCapture();

        assertThat(event.getResponseHeaderMap()).containsEntry("Testheader", "Testvalue1,Testvalue2");
    }

    private IAccessEvent logAndCapture() {
        requestLog.log(servletContextRequest, response);

        final ArgumentCaptor<IAccessEvent> captor = ArgumentCaptor.forClass(IAccessEvent.class);
        verify(appender, timeout(1000)).doAppend(captor.capture());

        return captor.getValue();
    }

    public class TestServletContextRequest extends ServletContextRequest {

        public TestServletContextRequest(ServletChannel servletChannel) {
            super(servletContextHandler.newServletContextApi(), servletChannel, request, response,
                null, null, null);
        }
    }
}
