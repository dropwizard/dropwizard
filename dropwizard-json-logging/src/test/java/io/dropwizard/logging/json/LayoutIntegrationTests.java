package io.dropwizard.logging.json;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.DeferredProcessingAware;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.logging.BootstrapLogging;
import io.dropwizard.logging.ConsoleAppenderFactory;
import io.dropwizard.logging.DefaultLoggingFactory;
import io.dropwizard.logging.json.layout.ExceptionFormat;
import io.dropwizard.logging.layout.DiscoverableLayoutFactory;
import io.dropwizard.request.logging.LogbackAccessRequestLogFactory;
import io.dropwizard.util.Resources;
import io.dropwizard.validation.BaseValidator;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Response;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static java.util.Objects.requireNonNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class LayoutIntegrationTests {

    static {
        BootstrapLogging.bootstrap(Level.INFO, new EventJsonLayoutBaseFactory());
    }

    private final ObjectMapper objectMapper = Jackson.newObjectMapper();

    @SuppressWarnings("rawtypes")
    private final YamlConfigurationFactory<ConsoleAppenderFactory> yamlFactory = new YamlConfigurationFactory<>(
        ConsoleAppenderFactory.class, BaseValidator.newValidator(), objectMapper, "dw-json-log");

    @Before
    public void setUp() {
        objectMapper.getSubtypeResolver().registerSubtypes(AccessJsonLayoutBaseFactory.class, EventJsonLayoutBaseFactory.class);
    }

    @SuppressWarnings("unchecked")
    private <T extends DeferredProcessingAware> ConsoleAppenderFactory<T> getAppenderFactory(String s) throws Exception {
        return yamlFactory.build(new File(Resources.getResource(s).toURI()));
    }

    @Test
    public void testDeserializeJson() throws Exception {
        ConsoleAppenderFactory<ILoggingEvent> appenderFactory = getAppenderFactory("yaml/json-log.yml");
        DiscoverableLayoutFactory<?> layout = requireNonNull(appenderFactory.getLayout());
        assertThat(layout).isInstanceOf(EventJsonLayoutBaseFactory.class);
        EventJsonLayoutBaseFactory factory = (EventJsonLayoutBaseFactory) layout;
        assertThat(factory).isNotNull();
        assertThat(factory.getTimestampFormat()).isEqualTo("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
        assertThat(factory.isPrettyPrint()).isFalse();
        assertThat(factory.isAppendLineSeparator()).isTrue();
        assertThat(factory.getIncludes()).contains(
            EventAttribute.LEVEL,
            EventAttribute.MDC,
            EventAttribute.MESSAGE,
            EventAttribute.LOGGER_NAME,
            EventAttribute.EXCEPTION,
            EventAttribute.TIMESTAMP);
        assertThat(factory.isFlattenMdc()).isTrue();
        assertThat(factory.getCustomFieldNames()).containsOnly(entry("timestamp", "@timestamp"));
        assertThat(factory.getAdditionalFields()).containsOnly(entry("service-name", "user-service"),
            entry("service-build", 218));
        assertThat(factory.getIncludesMdcKeys()).containsOnly("userId");

        ExceptionFormat exceptionFormat = requireNonNull(factory.getExceptionFormat());
        assertThat(exceptionFormat.getDepth()).isEqualTo("10");
        assertThat(exceptionFormat.isRootFirst()).isFalse();
        assertThat(exceptionFormat.getEvaluators()).contains("io.dropwizard");
    }

    @Test
    public void testDeserializeAccessJson() throws Exception {
        ConsoleAppenderFactory<IAccessEvent> appenderFactory = getAppenderFactory("yaml/json-access-log.yml");
        DiscoverableLayoutFactory<?> layout = requireNonNull(appenderFactory.getLayout());
        assertThat(layout).isInstanceOf(AccessJsonLayoutBaseFactory.class);
        AccessJsonLayoutBaseFactory factory = (AccessJsonLayoutBaseFactory) layout;
        assertThat(factory.getTimestampFormat()).isEqualTo("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        assertThat(factory.isPrettyPrint()).isFalse();
        assertThat(factory.isAppendLineSeparator()).isTrue();
        assertThat(factory.getIncludes()).contains(AccessAttribute.TIMESTAMP,
            AccessAttribute.REMOTE_USER,
            AccessAttribute.STATUS_CODE,
            AccessAttribute.METHOD,
            AccessAttribute.REQUEST_URL,
            AccessAttribute.REMOTE_HOST,
            AccessAttribute.REQUEST_PARAMETERS,
            AccessAttribute.REQUEST_CONTENT,
            AccessAttribute.TIMESTAMP,
            AccessAttribute.USER_AGENT);
        assertThat(factory.getResponseHeaders()).containsOnly("X-Request-Id");
        assertThat(factory.getRequestHeaders()).containsOnly("User-Agent", "X-Request-Id");
        assertThat(factory.getCustomFieldNames()).containsOnly(entry("statusCode", "status_code"),
            entry("userAgent", "user_agent"));
        assertThat(factory.getAdditionalFields()).containsOnly(entry("service-name", "shipping-service"),
            entry("service-version", "1.2.3"));
    }

    @Test
    public void testLogJsonToConsole() throws Exception {
        ConsoleAppenderFactory<ILoggingEvent> consoleAppenderFactory = getAppenderFactory("yaml/json-log-default.yml");
        DefaultLoggingFactory defaultLoggingFactory = new DefaultLoggingFactory();
        defaultLoggingFactory.setAppenders(Collections.singletonList(consoleAppenderFactory));

        DiscoverableLayoutFactory<?> layout = requireNonNull(consoleAppenderFactory.getLayout());
        assertThat(layout).isInstanceOf(EventJsonLayoutBaseFactory.class);
        EventJsonLayoutBaseFactory factory = (EventJsonLayoutBaseFactory) layout;
        assertThat(factory).isNotNull();
        assertThat(factory.getIncludes()).contains(EventAttribute.LEVEL,
            EventAttribute.THREAD_NAME,
            EventAttribute.MDC,
            EventAttribute.LOGGER_NAME,
            EventAttribute.MESSAGE,
            EventAttribute.EXCEPTION,
            EventAttribute.TIMESTAMP);
        assertThat(factory.isFlattenMdc()).isFalse();
        assertThat(factory.getIncludesMdcKeys()).isEmpty();
        assertThat(factory.getExceptionFormat()).isNull();

        PrintStream old = System.out;
        ByteArrayOutputStream redirectedStream = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(redirectedStream));
            defaultLoggingFactory.configure(new MetricRegistry(), "json-log-test");
            LoggerFactory.getLogger("com.example.app").info("Application log");
            Thread.sleep(100); // Need to wait, because the logger is async

            JsonNode jsonNode = objectMapper.readTree(redirectedStream.toString());
            assertThat(jsonNode).isNotNull();
            assertThat(jsonNode.get("timestamp").isTextual()).isTrue();
            assertThat(jsonNode.get("level").asText()).isEqualTo("INFO");
            assertThat(jsonNode.get("logger").asText()).isEqualTo("com.example.app");
            assertThat(jsonNode.get("message").asText()).isEqualTo("Application log");
        } finally {
            System.setOut(old);
        }
    }

    @Test
    public void testLogAccessJsonToConsole() throws Exception {
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
            when(request.getRemoteAddr()).thenReturn("10.0.0.1");
            when(request.getTimeStamp()).thenReturn(TimeUnit.SECONDS.toMillis(1353042047));
            when(request.getMethod()).thenReturn("GET");
            when(request.getRequestURI()).thenReturn("/test/users");
            when(request.getProtocol()).thenReturn("HTTP/1.1");
            when(request.getParameterNames()).thenReturn(Collections.enumeration(Arrays.asList("age", "city")));
            when(request.getParameterValues("age")).thenReturn(new String[]{"22"});
            when(request.getParameterValues("city")).thenReturn(new String[]{"LA"});
            when(request.getAttributeNames()).thenReturn(Collections.enumeration(Collections.emptyList()));
            when(request.getHeaderNames()).thenReturn(Collections.enumeration(Arrays.asList("Connection", "User-Agent")));
            when(request.getHeader("Connection")).thenReturn("keep-alive");
            when(request.getHeader("User-Agent")).thenReturn("Mozilla/5.0");

            Response response = mock(Response.class);
            when(response.getStatus()).thenReturn(200);
            when(response.getContentCount()).thenReturn(8290L);
            HttpFields httpFields = new HttpFields();
            httpFields.add("Date", "Mon, 16 Nov 2012 05:00:48 GMT");
            httpFields.add("Server", "Apache/2.4.12");
            when(response.getHttpFields()).thenReturn(httpFields);
            when(response.getHeaderNames()).thenReturn(Arrays.asList("Date", "Server"));
            when(response.getHeader("Date")).thenReturn("Mon, 16 Nov 2012 05:00:48 GMT");
            when(response.getHeader("Server")).thenReturn("Apache/2.4.12");

            requestLog.log(request, response);
            Thread.sleep(100); // Need to wait, because the logger is async

            JsonNode jsonNode = objectMapper.readTree(redirectedStream.toString());
            assertThat(jsonNode).isNotNull();
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
}
