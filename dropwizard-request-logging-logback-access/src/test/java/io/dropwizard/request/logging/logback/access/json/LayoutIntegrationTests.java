package io.dropwizard.request.logging.logback.access.json;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.classic.Level;
import ch.qos.logback.core.spi.DeferredProcessingAware;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.logging.common.BootstrapLogging;
import io.dropwizard.logging.common.ConsoleAppenderFactory;
import io.dropwizard.logging.json.EventJsonLayoutBaseFactory;
import io.dropwizard.request.logging.logback.access.LogbackAccessRequestLogFactory;
import io.dropwizard.validation.BaseValidator;
import org.eclipse.jetty.ee10.servlet.ServletChannel;
import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.ServletContextRequest;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.server.ConnectionMetaData;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class LayoutIntegrationTests {

    static {
        BootstrapLogging.bootstrap(Level.INFO, new EventJsonLayoutBaseFactory());
    }

    private final ObjectMapper objectMapper = Jackson.newObjectMapper();

    @SuppressWarnings("rawtypes")
    private final YamlConfigurationFactory<ConsoleAppenderFactory> yamlFactory = new YamlConfigurationFactory<>(
        ConsoleAppenderFactory.class, BaseValidator.newValidator(), objectMapper, "dw-json-log");

    @BeforeEach
    void setUp() {
        objectMapper.getSubtypeResolver().registerSubtypes(AccessJsonLayoutBaseFactory.class, EventJsonLayoutBaseFactory.class);
    }

    @SuppressWarnings("unchecked")
    private <T extends DeferredProcessingAware> ConsoleAppenderFactory<T> getAppenderFactory(String s) throws Exception {
        return yamlFactory.build(new ResourceConfigurationSourceProvider(), s);
    }

    @Test
    void testDeserializeAccessJson() throws Exception {
        assertThat(getAppenderFactory("yaml/json-access-log.yml"))
            .extracting(ConsoleAppenderFactory::getLayout)
            .isInstanceOfSatisfying(AccessJsonLayoutBaseFactory.class, accessJsonLayoutBaseFactory -> assertThat(accessJsonLayoutBaseFactory)
                .satisfies(factory -> assertThat(factory.getTimestampFormat()).isEqualTo("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"))
                .satisfies(factory -> assertThat(factory.isPrettyPrint()).isFalse())
                .satisfies(factory -> assertThat(factory.isAppendLineSeparator()).isTrue())
                .satisfies(factory -> assertThat(factory.getIncludes()).contains(
                    AccessAttribute.TIMESTAMP,
                    AccessAttribute.REMOTE_USER,
                    AccessAttribute.STATUS_CODE,
                    AccessAttribute.METHOD,
                    AccessAttribute.REQUEST_URL,
                    AccessAttribute.REMOTE_HOST,
                    AccessAttribute.REQUEST_PARAMETERS,
                    AccessAttribute.REQUEST_CONTENT,
                    AccessAttribute.TIMESTAMP,
                    AccessAttribute.USER_AGENT,
                    AccessAttribute.PATH_QUERY))
                .satisfies(factory -> assertThat(factory.getResponseHeaders()).containsOnly("X-Request-Id"))
                .satisfies(factory -> assertThat(factory.getRequestHeaders()).containsOnly("User-Agent", "X-Request-Id"))
                .satisfies(factory -> assertThat(factory.getCustomFieldNames()).containsOnly(
                    entry("statusCode", "status_code"),
                    entry("userAgent", "user_agent")))
                .satisfies(factory -> assertThat(factory.getAdditionalFields()).containsOnly(
                    entry("service-name", "shipping-service"),
                    entry("service-version", "1.2.3"))));
    }

    @Test
    void testLogAccessJsonToConsole() throws Exception {
        ConsoleAppenderFactory<IAccessEvent> consoleAppenderFactory = getAppenderFactory("yaml/json-access-log-default.yml");
        // Use sys.err, because there are some other log configuration messages in std.out
        consoleAppenderFactory.setTarget(ConsoleAppenderFactory.ConsoleStream.STDERR);

        final LogbackAccessRequestLogFactory requestLogHandler = new LogbackAccessRequestLogFactory();
        requestLogHandler.setAppenders(Collections.singletonList(consoleAppenderFactory));

        PrintStream old = System.err;
        ByteArrayOutputStream redirectedStream = new ByteArrayOutputStream();
        try {
            System.setErr(new PrintStream(redirectedStream));
            RequestLog requestLog = requestLogHandler.build("json-access-log-test");

            Request request = mock(Request.class);
            Response response = mock(Response.class);
            ConnectionMetaData connectionMetaData = mock(ConnectionMetaData.class);
            HttpURI httpURI = mock(HttpURI.class);

            when(response.getHeaders()).thenReturn(HttpFields.build());
            when(request.getConnectionMetaData()).thenReturn(connectionMetaData);

            when(httpURI.getPath()).thenReturn("/test/users");
            when(httpURI.getQuery()).thenReturn("age=22&city=LA");
            when(httpURI.getAuthority()).thenReturn("10.0.0.1");
            when(request.getHttpURI()).thenReturn(httpURI);

            when(connectionMetaData.getHttpConfiguration()).thenReturn(new HttpConfiguration());
            when(connectionMetaData.getProtocol()).thenReturn("HTTP/1.1");

            HttpFields requestHeaders = HttpFields.build()
                .add("Connection", "keep-alive")
                .add("User-Agent", "Mozilla/5.0");
            when(request.getHeaders()).thenReturn(requestHeaders);
            when(request.getAttributeNameSet()).thenReturn(Set.of());
            when(request.getMethod()).thenReturn("GET");

            HttpFields.Mutable responseHeaders = HttpFields.build()
                .add("Date", "Mon, 16 Nov 2012 05:00:48 GMT")
                .add("Server", "Apache/2.4.12");
            when(response.getHeaders()).thenReturn(responseHeaders);
            when(response.getStatus()).thenReturn(200);

            final ServletContextHandler servletContextHandler = new ServletContextHandler();
            final ServletChannel servletChannel = new ServletChannel(servletContextHandler, connectionMetaData);
            ServletContextRequest servletContextRequest = new TestServletContextRequest(servletContextHandler, servletChannel, request, response);
            servletChannel.associate(servletContextRequest);

            try (MockedStatic<Request> staticRequest = mockStatic(Request.class, CALLS_REAL_METHODS); MockedStatic<Response> staticResponse = mockStatic(Response.class)) {
                staticRequest.when(() -> Request.getRemoteAddr(servletContextRequest)).thenReturn("10.0.0.1");
                staticRequest.when(() -> Request.getTimeStamp(servletContextRequest)).thenReturn(TimeUnit.SECONDS.toMillis(1353042047));
                staticResponse.when(() -> Response.getContentBytesWritten(response)).thenReturn(8290L);

                requestLog.log(servletContextRequest, response);
                // Need to wait, because the logger is async
                await().atMost(1, TimeUnit.SECONDS).until(() -> !redirectedStream.toString().isEmpty());
            }

            JsonNode jsonNode = objectMapper.readTree(redirectedStream.toString());
            assertThat(jsonNode.fieldNames().next()).isEqualTo("timestamp");
            assertThat(jsonNode.get("timestamp").isNumber()).isTrue();
            assertThat(jsonNode.get("requestTime").isNumber()).isTrue();
            assertThat(jsonNode.get("remoteAddress").asText()).isEqualTo("10.0.0.1");
            assertThat(jsonNode.get("status").asInt()).isEqualTo(200);
            assertThat(jsonNode.get("method").asText()).isEqualTo("GET");
            assertThat(jsonNode.get("uri").asText()).isEqualTo("/test/users");
            assertThat(jsonNode.get("protocol").asText()).isEqualTo("HTTP/1.1");
            assertThat(jsonNode.get("userAgent").asText()).isEqualTo("Mozilla/5.0");
            assertThat(jsonNode.get("contentLength").asInt()).isEqualTo(8290);
        } finally {
            System.setErr(old);
        }

    }

    public static class TestServletContextRequest extends ServletContextRequest {

        public TestServletContextRequest(ServletContextHandler servletContextHandler, ServletChannel servletChannel, Request request, Response response) {
            super(servletContextHandler.newServletContextApi(), servletChannel, request, response,
                null, null, null);
        }

        @Override
        public Set<String> getAttributeNameSet() {
            return Set.of();
        }
    }
}
