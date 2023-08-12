package io.dropwizard.request.logging.old;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.logging.common.BootstrapLogging;
import io.dropwizard.logging.common.ConsoleAppenderFactory;
import io.dropwizard.logging.common.FileAppenderFactory;
import io.dropwizard.logging.common.SyslogAppenderFactory;
import io.dropwizard.request.logging.RequestLogFactory;
import io.dropwizard.validation.BaseValidator;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.server.ConnectionMetaData;
import org.eclipse.jetty.server.CustomRequestLog;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.internal.HttpChannelState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.util.Collections;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class LogbackClassicRequestLogFactoryTest {

    static {
        BootstrapLogging.bootstrap();
    }

    private RequestLogFactory<?> requestLog;

    @BeforeEach
    void setUp() throws Exception {
        final ObjectMapper objectMapper = Jackson.newObjectMapper();
        objectMapper.getSubtypeResolver().registerSubtypes(ConsoleAppenderFactory.class, FileAppenderFactory.class,
            SyslogAppenderFactory.class);
        this.requestLog = new YamlConfigurationFactory<>(RequestLogFactory.class,
            BaseValidator.newValidator(), objectMapper, "dw")
            .build(new ResourceConfigurationSourceProvider(), "yaml/logbackClassicRequestLog.yml");
    }

    @Test
    void testDeserialized() {
        assertThat(requestLog)
            .isInstanceOfSatisfying(LogbackClassicRequestLogFactory.class, logFactory -> assertThat(logFactory)
                .satisfies(classicRequestLogFactory -> assertThat(classicRequestLogFactory.getTimeZone()).isEqualTo(TimeZone.getTimeZone("Europe/Amsterdam")))
                .satisfies(classicRequestLogFactory -> assertThat(classicRequestLogFactory.getAppenders()).hasSize(3).extractingResultOf("getClass")
                    .containsOnly(ConsoleAppenderFactory.class, FileAppenderFactory.class, SyslogAppenderFactory.class
        )));
    }

    @Test
    void testLogFormat() throws Exception {
        final LogbackClassicRequestLogFactory factory = new LogbackClassicRequestLogFactory();

        @SuppressWarnings("unchecked")
        final Appender<ILoggingEvent> appender = mock(Appender.class);
        final Request request = mock(Request.class);
        final Response response = mock(Response.class, RETURNS_DEEP_STUBS);
        final HttpChannelState channelState = mock(HttpChannelState.class);

        factory.setAppenders(Collections.singletonList(
            (context, applicationName, layoutFactory, levelFilterFactory, asyncAppenderFactory) -> appender));
        final String tz = TimeZone.getAvailableIDs((int)TimeUnit.HOURS.toMillis(-5))[0];
        factory.setTimeZone(TimeZone.getTimeZone(tz));

        CustomRequestLog logger = null;
        try (MockedStatic<Request> staticRequest = mockStatic(Request.class);
             MockedStatic<Response> staticResponse = mockStatic(Response.class)) {
            staticRequest.when(() -> Request.getRemoteAddr(request)).thenReturn("10.0.0.1");

            // Jetty log format compares against System.currentTimeMillis, so there
            // isn't a way for us to set our own clock
            when(request.getHeadersNanoTime()).thenReturn(0L);
            when(request.getMethod()).thenReturn("GET");
            HttpURI httpURI = mock(HttpURI.class);
            when(httpURI.getPath()).thenReturn("/test/things");
            when(request.getHttpURI()).thenReturn(httpURI);
            ConnectionMetaData connectionMetaData = mock(ConnectionMetaData.class);
            when(connectionMetaData.getProtocol()).thenReturn("HTTP/1.1");
            when(request.getConnectionMetaData()).thenReturn(connectionMetaData);
            when(request.getHeaders()).thenReturn(HttpFields.build());

            staticResponse.when(() -> Response.getContentBytesWritten(response)).thenReturn(8290L);

            final ArgumentCaptor<ILoggingEvent> captor = ArgumentCaptor.forClass(ILoggingEvent.class);

            logger = (CustomRequestLog)factory.build("my-app");
            logger.log(request, response);
            verify(appender, timeout(1000)).doAppend(captor.capture());

            final ILoggingEvent event = captor.getValue();
            assertThat(event.getFormattedMessage())
                .startsWith("10.0.0.1")
                .doesNotContain("%")
                .contains("\"GET /test/things HTTP/1.1\"")
                .contains("-0500");
        } catch (Exception e) {
            if (logger != null) {
                logger.stop();
            }
            throw e;
        }
    }

    @Test
    void isDiscoverable() {
        assertThat(new DiscoverableSubtypeResolver().getDiscoveredSubtypes())
            .contains(LogbackClassicRequestLogFactory.class);
    }
}
