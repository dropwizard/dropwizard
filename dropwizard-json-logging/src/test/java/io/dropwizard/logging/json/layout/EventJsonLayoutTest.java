package io.dropwizard.logging.json.layout;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggerContextVO;
import ch.qos.logback.classic.spi.ThrowableProxyVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.logging.json.EventAttribute;
import io.dropwizard.util.Maps;
import io.dropwizard.util.Sets;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Marker;

import java.time.ZoneId;
import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EventJsonLayoutTest {
    private static final String timestamp = "2018-01-02T15:19:21.000+0000";
    private static final String logger = "com.example.user.service";
    private static final String message = "User[18] has been registered";
    private static final Map<String, String> mdc = Maps.of(
            "userId", "18",
            "serviceId", "19",
            "orderId", "24");

    private static final Set<EventAttribute> DEFAULT_EVENT_ATTRIBUTES = Collections.unmodifiableSet(EnumSet.of(
            EventAttribute.LEVEL,
            EventAttribute.THREAD_NAME,
            EventAttribute.MDC,
            EventAttribute.MARKER,
            EventAttribute.LOGGER_NAME,
            EventAttribute.MESSAGE,
            EventAttribute.EXCEPTION,
            EventAttribute.TIMESTAMP,
            EventAttribute.CALLER_DATA));

    private final TimestampFormatter timestampFormatter = new TimestampFormatter("yyyy-MM-dd'T'HH:mm:ss.SSSZ", ZoneId.of("UTC"));
    private final JsonFormatter jsonFormatter = new JsonFormatter(Jackson.newObjectMapper(), false, true);
    private ThrowableProxyConverter throwableProxyConverter = Mockito.mock(ThrowableProxyConverter.class);
    private ILoggingEvent event = Mockito.mock(ILoggingEvent.class);
    private Marker marker = Mockito.mock(Marker.class);
    private Map<String, Object> defaultExpectedFields;

    private EventJsonLayout eventJsonLayout;

    @BeforeEach
    public void setUp() {
        when(event.getTimeStamp()).thenReturn(1514906361000L);
        when(event.getLevel()).thenReturn(Level.INFO);
        when(event.getThreadName()).thenReturn("main");
        when(event.getMDCPropertyMap()).thenReturn(mdc);
        when(event.getMarker()).thenReturn(marker);
        when(event.getLoggerName()).thenReturn(logger);
        when(event.getFormattedMessage()).thenReturn(message);
        when(event.getLoggerContextVO()).thenReturn(new LoggerContextVO("test", Collections.emptyMap(), 0));
        when(event.getCallerData()).thenReturn(new StackTraceElement[]{
                new StackTraceElement("declaringClass", "methodName", "fileName", 42)
        });

        when(marker.getName()).thenReturn("marker");

        eventJsonLayout = new EventJsonLayout(jsonFormatter, timestampFormatter, throwableProxyConverter,
                DEFAULT_EVENT_ATTRIBUTES, Collections.emptyMap(), Collections.emptyMap(), Collections.emptySet(), false);

        defaultExpectedFields = new HashMap<>();
        defaultExpectedFields.put("timestamp", timestamp);
        defaultExpectedFields.put("logger", logger);
        defaultExpectedFields.put("marker", "marker");
        defaultExpectedFields.put("message", message);
        defaultExpectedFields.put("thread", "main");
        defaultExpectedFields.put("level", "INFO");
        defaultExpectedFields.put("mdc", mdc);
        defaultExpectedFields.put("caller_class_name", "declaringClass");
        defaultExpectedFields.put("caller_file_name", "fileName");
        defaultExpectedFields.put("caller_line_number", 42);
        defaultExpectedFields.put("caller_method_name", "methodName");
    }

    @Test
    public void testProducesDefaultMap() {
        Map<String, Object> map = eventJsonLayout.toJsonMap(event);
        final HashMap<String, Object> expectedFields = new HashMap<>(defaultExpectedFields);
        assertThat(map).isEqualTo(expectedFields);
    }

    @Test
    public void testLogsAnException() {
        when(event.getThrowableProxy()).thenReturn(new ThrowableProxyVO());
        when(throwableProxyConverter.convert(event)).thenReturn("Boom!");

        final HashMap<String, Object> expectedFields = new HashMap<>(defaultExpectedFields);
        expectedFields.put("exception", "Boom!");
        assertThat(eventJsonLayout.toJsonMap(event)).isEqualTo(expectedFields);
    }

    @Test
    public void testDisableTimestamp() {
        EnumSet<EventAttribute> eventAttributes = EnumSet.copyOf(DEFAULT_EVENT_ATTRIBUTES);
        eventAttributes.remove(EventAttribute.TIMESTAMP);
        eventJsonLayout.setIncludes(eventAttributes);

        final HashMap<String, Object> expectedFields = new HashMap<>(defaultExpectedFields);
        expectedFields.remove("timestamp");
        assertThat(eventJsonLayout.toJsonMap(event)).isEqualTo(expectedFields);
    }

    @Test
    public void testLogVersion() {
        eventJsonLayout.setJsonProtocolVersion("1.2");

        final HashMap<String, Object> expectedFields = new HashMap<>(defaultExpectedFields);
        expectedFields.put("version", "1.2");
        assertThat(eventJsonLayout.toJsonMap(event)).isEqualTo(expectedFields);
    }

    @Test
    public void testReplaceFieldName() {
        final Map<String, String> customFieldNames = Maps.of(
                "timestamp", "@timestamp",
                "message", "@message");
        Map<String, Object> map = new EventJsonLayout(jsonFormatter, timestampFormatter, throwableProxyConverter, DEFAULT_EVENT_ATTRIBUTES,
                customFieldNames, Collections.emptyMap(), Collections.emptySet(), false)
            .toJsonMap(event);

        final HashMap<String, Object> expectedFields = new HashMap<>(defaultExpectedFields);
        expectedFields.put("@timestamp", timestamp);
        expectedFields.put("@message", message);
        expectedFields.remove("timestamp");
        expectedFields.remove("message");
        assertThat(map).isEqualTo(expectedFields);
    }

    @Test
    public void testAddNewField() {
        final Map<String, Object> additionalFields = Maps.of(
                "serviceName", "userService",
                "serviceBuild", 207);
        Map<String, Object> map = new EventJsonLayout(jsonFormatter, timestampFormatter, throwableProxyConverter, DEFAULT_EVENT_ATTRIBUTES,
            Collections.emptyMap(), additionalFields,
            Collections.emptySet(), false)
            .toJsonMap(event);

        final HashMap<String, Object> expectedFields = new HashMap<>(defaultExpectedFields);
        expectedFields.put("serviceName", "userService");
        expectedFields.put("serviceBuild", 207);
        assertThat(map).isEqualTo(expectedFields);
    }

    @Test
    public void testFilterMdc() {
        final Set<String> includesMdcKeys = Sets.of("userId", "orderId");
        Map<String, Object> map = new EventJsonLayout(jsonFormatter, timestampFormatter, throwableProxyConverter, DEFAULT_EVENT_ATTRIBUTES,
            Collections.emptyMap(), Collections.emptyMap(), includesMdcKeys, false)
                .toJsonMap(event);

        final Map<String, String> expectedMdc = Maps.of(
                "userId", "18",
                "orderId", "24");
        final HashMap<String, Object> expectedFields = new HashMap<>(defaultExpectedFields);
        expectedFields.put("mdc", expectedMdc);
        assertThat(map).isEqualTo(expectedFields);
    }

    @Test
    public void testFlattensMdcMap() {
        Map<String, Object> map = new EventJsonLayout(jsonFormatter, timestampFormatter, throwableProxyConverter,
                DEFAULT_EVENT_ATTRIBUTES, Collections.emptyMap(), Collections.emptyMap(), Collections.emptySet(), true)
                .toJsonMap(event);

        final HashMap<String, Object> expectedFields = new HashMap<>(defaultExpectedFields);
        expectedFields.putAll(mdc);
        expectedFields.remove("mdc");
        assertThat(map).isEqualTo(expectedFields);
    }

    @Test
    public void testStartThrowableConverter() {
        eventJsonLayout.start();

        verify(throwableProxyConverter).start();
    }

    @Test
    public void testStopThrowableConverter() {
        eventJsonLayout.stop();

        verify(throwableProxyConverter).stop();
    }
}
