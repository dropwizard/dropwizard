package io.dropwizard.logging.json;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.DeferredProcessingAware;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.ConfigurationValidationException;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.logging.common.BootstrapLogging;
import io.dropwizard.logging.common.ConsoleAppenderFactory;
import io.dropwizard.logging.common.DefaultLoggingFactory;
import io.dropwizard.request.logging.LogbackAccessRequestLogFactory;
import io.dropwizard.validation.BaseValidator;
import org.eclipse.jetty.http.HttpFields;
import org.eclipse.jetty.http.MetaData;
import org.eclipse.jetty.server.HttpChannel;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.RequestLog;
import org.eclipse.jetty.server.Response;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import jakarta.validation.constraints.Min;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.entry;
import static org.awaitility.Awaitility.await;
import static org.mockito.Mockito.mock;
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
    void testDeserializeJson() throws Exception {
        assertThat(getAppenderFactory("yaml/json-log.yml"))
            .extracting(ConsoleAppenderFactory::getLayout)
            .isInstanceOfSatisfying(EventJsonLayoutBaseFactory.class, eventJsonLayoutBaseFactory -> assertThat(eventJsonLayoutBaseFactory)
                .satisfies(factory -> assertThat(factory).isNotNull())
                .satisfies(factory -> assertThat(factory.getTimestampFormat()).isEqualTo("yyyy-MM-dd'T'HH:mm:ss.SSSZ"))
                .satisfies(factory -> assertThat(factory.isPrettyPrint()).isFalse())
                .satisfies(factory -> assertThat(factory.isAppendLineSeparator()).isTrue())
                .satisfies(factory -> assertThat(factory.getIncludes()).contains(
                    EventAttribute.LEVEL,
                    EventAttribute.MDC,
                    EventAttribute.MESSAGE,
                    EventAttribute.LOGGER_NAME,
                    EventAttribute.EXCEPTION,
                    EventAttribute.TIMESTAMP,
                    EventAttribute.CALLER_DATA))
                .satisfies(factory -> assertThat(factory.isFlattenMdc()).isTrue())
                .satisfies(factory -> assertThat(factory.getCustomFieldNames()).containsOnly(entry("timestamp", "@timestamp")))
                .satisfies(factory -> assertThat(factory.getAdditionalFields()).containsOnly(
                    entry("service-name", "user-service"),
                    entry("service-build", 218)))
                .satisfies(factory -> assertThat(factory.getIncludesMdcKeys()).containsOnly("userId"))
                .extracting(EventJsonLayoutBaseFactory::getExceptionFormat)
                .satisfies(exceptionFormat -> assertThat(exceptionFormat.getDepth()).isEqualTo("10"))
                .satisfies(exceptionFormat -> assertThat(exceptionFormat.isRootFirst()).isFalse())
                .satisfies(exceptionFormat -> assertThat(exceptionFormat.getEvaluators()).contains("io.dropwizard")));
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
    void testLogJsonToConsole() throws Exception {
        ConsoleAppenderFactory<ILoggingEvent> consoleAppenderFactory = getAppenderFactory("yaml/json-log-default.yml");
        DefaultLoggingFactory defaultLoggingFactory = new DefaultLoggingFactory();
        defaultLoggingFactory.setAppenders(Collections.singletonList(consoleAppenderFactory));

        assertThat(consoleAppenderFactory.getLayout())
            .isInstanceOfSatisfying(EventJsonLayoutBaseFactory.class, eventJsonLayoutBaseFactory -> assertThat(eventJsonLayoutBaseFactory)
                .satisfies(factory -> assertThat(factory).isNotNull())
                .satisfies(factory -> assertThat(factory.getIncludes()).contains(
                    EventAttribute.LEVEL,
                    EventAttribute.THREAD_NAME,
                    EventAttribute.MDC,
                    EventAttribute.MARKER,
                    EventAttribute.LOGGER_NAME,
                    EventAttribute.MESSAGE,
                    EventAttribute.EXCEPTION,
                    EventAttribute.TIMESTAMP))
                .satisfies(factory -> assertThat(factory.isFlattenMdc()).isFalse())
                .satisfies(factory -> assertThat(factory.getIncludesMdcKeys()).isEmpty())
                .satisfies(factory -> assertThat(factory.getExceptionFormat()).isNull()));

        PrintStream old = System.out;
        ByteArrayOutputStream redirectedStream = new ByteArrayOutputStream();
        try {
            System.setOut(new PrintStream(redirectedStream));
            defaultLoggingFactory.configure(new MetricRegistry(), "json-log-test");
            Marker marker = MarkerFactory.getMarker("marker");
            LoggerFactory.getLogger("com.example.app").info(marker, "Application log");
            // Need to wait, because the logger is async
            await().atMost(1, TimeUnit.SECONDS).until(() -> !redirectedStream.toString().isEmpty());

            JsonNode jsonNode = objectMapper.readTree(redirectedStream.toString());
            assertThat(jsonNode.fieldNames().next()).isEqualTo("timestamp");
            assertThat(jsonNode.get("timestamp").isTextual()).isTrue();
            assertThat(jsonNode.get("level").asText()).isEqualTo("INFO");
            assertThat(jsonNode.get("logger").asText()).isEqualTo("com.example.app");
            assertThat(jsonNode.get("marker").asText()).isEqualTo("marker");
            assertThat(jsonNode.get("message").asText()).isEqualTo("Application log");
        } finally {
            System.setOut(old);
        }
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
            MetaData.Response metaData = mock(MetaData.Response.class);
            when(metaData.getStatus()).thenReturn(200);
            when(response.getCommittedMetaData()).thenReturn(metaData);
            HttpChannel channel = mock(HttpChannel.class);
            when(channel.getBytesWritten()).thenReturn(8290L);
            when(response.getHttpChannel()).thenReturn(channel);
            HttpFields.Mutable httpFields = HttpFields.build();
            httpFields.add("Date", "Mon, 16 Nov 2012 05:00:48 GMT");
            httpFields.add("Server", "Apache/2.4.12");
            when(response.getHttpFields()).thenReturn(httpFields);
            when(response.getHeaderNames()).thenReturn(Arrays.asList("Date", "Server"));
            when(response.getHeader("Date")).thenReturn("Mon, 16 Nov 2012 05:00:48 GMT");
            when(response.getHeader("Server")).thenReturn("Apache/2.4.12");

            requestLog.log(request, response);
            // Need to wait, because the logger is async
            await().atMost(1, TimeUnit.SECONDS).until(() -> !redirectedStream.toString().isEmpty());

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

    @Test
    void invalidJsonLogLayoutField() {
        assertThatExceptionOfType(ConfigurationValidationException.class)
            .isThrownBy(() -> getAppenderFactory("yaml/custom-json-log-invalid.yml"))
            .withMessageContaining("messageSize must be greater than or equal to 1");
    }

    @JsonTypeName("custom-json")
    public static class CustomJsonLayoutBaseFactory extends EventJsonLayoutBaseFactory {

        @JsonProperty
        @Min(1)
        private int messageSize = 8000;
    }
}
