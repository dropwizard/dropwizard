package io.dropwizard.logging.json.layout;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.pattern.ThrowableProxyConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggerContextVO;
import ch.qos.logback.classic.spi.ThrowableProxyVO;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.logging.json.EventAttribute;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.ZoneId;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class EventJsonLayoutTest {

    private String timestamp = "2018-01-02T15:19:21.000+0000";
    private String logger = "com.example.user.service";
    private String message = "User[18] has been registered";
    private Map<String, String> mdc = ImmutableMap.of("userId", "18", "serviceId", "19", "orderId", "24");
    private ThrowableProxyConverter throwableProxyConverter = Mockito.mock(ThrowableProxyConverter.class);
    private TimestampFormatter timestampFormatter = new TimestampFormatter("yyyy-MM-dd'T'HH:mm:ss.SSSZ",
        ZoneId.of("UTC"));
    private ObjectMapper objectMapper = Jackson.newObjectMapper();
    private JsonFormatter jsonFormatter = new JsonFormatter(objectMapper, false, true);
    private ILoggingEvent event = Mockito.mock(ILoggingEvent.class);

    private Set<EventAttribute> includes = EnumSet.of(EventAttribute.LEVEL,
        EventAttribute.THREAD_NAME, EventAttribute.MDC, EventAttribute.LOGGER_NAME, EventAttribute.MESSAGE,
        EventAttribute.EXCEPTION, EventAttribute.TIMESTAMP);
    private EventJsonLayout eventJsonLayout = new EventJsonLayout(jsonFormatter, timestampFormatter, throwableProxyConverter,
        includes, ImmutableMap.of(), ImmutableMap.of(), ImmutableSet.of(), false);

    @Before
    public void setUp() {
        when(event.getTimeStamp()).thenReturn(1514906361000L);
        when(event.getLevel()).thenReturn(Level.INFO);
        when(event.getThreadName()).thenReturn("main");
        when(event.getMDCPropertyMap()).thenReturn(mdc);
        when(event.getLoggerName()).thenReturn(logger);
        when(event.getFormattedMessage()).thenReturn(message);
        when(event.getLoggerContextVO()).thenReturn(new LoggerContextVO("test", ImmutableMap.of(), 0));
    }

    @Test
    public void testProducesDefaultMap() {
        Map<String, Object> map = eventJsonLayout.toJsonMap(event);
        assertThat(map).containsOnly(entry("timestamp", timestamp),
            entry("thread", "main"),
            entry("level", "INFO"),
            entry("logger", logger),
            entry("message", message),
            entry("mdc", mdc));
    }

    @Test
    public void testLogsAnException() {
        when(event.getThrowableProxy()).thenReturn(new ThrowableProxyVO());
        when(throwableProxyConverter.convert(event)).thenReturn("Boom!");

        assertThat(eventJsonLayout.toJsonMap(event)).containsOnly(entry("timestamp", timestamp),
            entry("thread", "main"),
            entry("level", "INFO"),
            entry("logger", logger),
            entry("message", message),
            entry("mdc", mdc),
            entry("exception", "Boom!"));
    }

    @Test
    public void testDisableTimestamp() {
        includes.remove(EventAttribute.TIMESTAMP);
        eventJsonLayout.setIncludes(includes);

        assertThat(eventJsonLayout.toJsonMap(event)).containsOnly(
            entry("thread", "main"),
            entry("level", "INFO"),
            entry("logger", logger),
            entry("message", message),
            entry("mdc", mdc));
    }

    @Test
    public void testLogVersion() {
        eventJsonLayout.setJsonProtocolVersion("1.2");

        assertThat(eventJsonLayout.toJsonMap(event)).containsOnly(entry("timestamp", timestamp),
            entry("thread", "main"),
            entry("level", "INFO"),
            entry("logger", logger),
            entry("message", message),
            entry("mdc", mdc),
            entry("version", "1.2"));
    }

    @Test
    public void testReplaceFieldName() {
        Map<String, Object> map = new EventJsonLayout(jsonFormatter, timestampFormatter, throwableProxyConverter, includes,
            ImmutableMap.of("timestamp", "@timestamp", "message", "@message"), ImmutableMap.of(),
            ImmutableSet.of(), false)
            .toJsonMap(event);
        assertThat(map).containsOnly(entry("@timestamp", timestamp),
            entry("thread", "main"),
            entry("level", "INFO"),
            entry("logger", logger),
            entry("@message", message),
            entry("mdc", mdc));
    }

    @Test
    public void testAddNewField() {
        Map<String, Object> map = new EventJsonLayout(jsonFormatter, timestampFormatter, throwableProxyConverter, includes,
            ImmutableMap.of(), ImmutableMap.of("serviceName", "userService", "serviceBuild", 207),
            ImmutableSet.of(), false)
            .toJsonMap(event);
        assertThat(map).containsOnly(entry("timestamp", timestamp),
            entry("thread", "main"),
            entry("level", "INFO"),
            entry("logger", logger),
            entry("message", message),
            entry("mdc", mdc),
            entry("serviceName", "userService"),
            entry("serviceBuild", 207));
    }

    @Test
    public void testFilterMdc() {
        Map<String, Object> map = new EventJsonLayout(jsonFormatter, timestampFormatter, throwableProxyConverter, includes,
            ImmutableMap.of(), ImmutableMap.of(),
            ImmutableSet.of("userId", "orderId"), false).toJsonMap(event);
        assertThat(map).containsOnly(entry("timestamp", timestamp),
            entry("thread", "main"),
            entry("level", "INFO"),
            entry("logger", logger),
            entry("message", message),
            entry("mdc", ImmutableMap.of("userId", "18", "orderId", "24")));
    }

    @Test
    public void testFlattensMdcMap() {
        Map<String, Object> map = new EventJsonLayout(jsonFormatter, timestampFormatter, throwableProxyConverter,
            includes, ImmutableMap.of(), ImmutableMap.of(), ImmutableSet.of(), true).toJsonMap(event);
        assertThat(map).containsOnly(entry("timestamp", timestamp),
                                     entry("thread", "main"),
                                     entry("level", "INFO"),
                                     entry("logger", logger),
                                     entry("message", message),
                                     entry("userId", "18"),
                                     entry("serviceId", "19"),
                                     entry("orderId", "24"));
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
