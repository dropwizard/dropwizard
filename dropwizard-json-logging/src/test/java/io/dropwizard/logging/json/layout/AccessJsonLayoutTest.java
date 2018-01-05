package io.dropwizard.logging.json.layout;

import ch.qos.logback.access.spi.IAccessEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.logging.json.AccessAttribute;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;

import java.time.ZoneId;
import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.entry;
import static org.mockito.Mockito.when;

public class AccessJsonLayoutTest {

    private String remoteHost = "nw-4.us.crawl.io";
    private String serverName = "sw-2.us.api.example.io";
    private String timestamp = "2018-01-01T14:35:21.000+0000";
    private String uri = "/test/users?age=22&city=LA";
    private String url = "GET /test/users?age=22&city=LA HTTP/1.1";
    private String userAgent = "Mozilla/5.0";
    private Map<String, String> requestHeaders = ImmutableMap.of("Host", "api.example.io",
        "User-Agent", userAgent);
    private Map<String, String> responseHeaders = ImmutableMap.of("Content-Type", "application/json",
        "Transfer-Encoding", "chunked");
    private String responseContent = "{\"message\":\"Hello, Crawler!\"}";
    private String remoteAddress = "192.168.52.15";
    private IAccessEvent event = Mockito.mock(IAccessEvent.class);

    private TimestampFormatter timestampFormatter = new TimestampFormatter("yyyy-MM-dd'T'HH:mm:ss.SSSZ", ZoneId.of("UTC"));
    private ObjectMapper objectMapper = Jackson.newObjectMapper();
    private JsonFormatter jsonFormatter = new JsonFormatter(objectMapper, false, true);
    private Set<AccessAttribute> includes = EnumSet.of(AccessAttribute.REMOTE_ADDRESS,
        AccessAttribute.REMOTE_USER, AccessAttribute.REQUEST_TIME, AccessAttribute.REQUEST_URI,
        AccessAttribute.STATUS_CODE, AccessAttribute.METHOD, AccessAttribute.PROTOCOL, AccessAttribute.CONTENT_LENGTH,
        AccessAttribute.USER_AGENT, AccessAttribute.TIMESTAMP);
    private AccessJsonLayout accessJsonLayout = new AccessJsonLayout(jsonFormatter, timestampFormatter,
        includes, ImmutableMap.of(), ImmutableMap.of());

    @Before
    public void setUp() {
        when(event.getTimeStamp()).thenReturn(1514817321000L);
        when(event.getContentLength()).thenReturn(78L);
        when(event.getLocalPort()).thenReturn(8080);
        when(event.getMethod()).thenReturn("GET");
        when(event.getProtocol()).thenReturn("HTTP/1.1");
        when(event.getRequestContent()).thenReturn("");
        when(event.getRemoteAddr()).thenReturn(remoteAddress);
        when(event.getRemoteUser()).thenReturn("john");
        when(event.getRequestHeaderMap()).thenReturn(requestHeaders);
        when(event.getRequestParameterMap()).thenReturn(ImmutableMap.of());
        when(event.getElapsedTime()).thenReturn(100L);
        when(event.getRequestURI()).thenReturn(uri);
        when(event.getRequestURL()).thenReturn(url);
        when(event.getRemoteHost()).thenReturn(remoteHost);
        when(event.getResponseContent()).thenReturn(responseContent);
        when(event.getResponseHeaderMap()).thenReturn(responseHeaders);
        when(event.getServerName()).thenReturn(serverName);
        when(event.getStatusCode()).thenReturn(200);
        when(event.getRequestHeader("User-Agent")).thenReturn(userAgent);
        accessJsonLayout.setIncludes(includes);
    }

    @Test
    public void testProducesDefaultJsonMap() {
        assertThat(accessJsonLayout.toJsonMap(event)).containsOnly(
            entry("timestamp", timestamp), entry("remoteUser", "john"),
            entry("method", "GET"), entry("uri", uri),
            entry("protocol", "HTTP/1.1"), entry("status", 200),
            entry("requestTime", 100L), entry("contentLength", 78L),
            entry("userAgent", userAgent), entry("remoteAddress", remoteAddress));
    }

    @Test
    public void testDisableRemoteAddress() {
        includes.remove(AccessAttribute.REMOTE_ADDRESS);
        accessJsonLayout.setIncludes(includes);

        assertThat(accessJsonLayout.toJsonMap(event)).containsOnly(
            entry("timestamp", timestamp), entry("remoteUser", "john"),
            entry("method", "GET"), entry("uri", uri),
            entry("protocol", "HTTP/1.1"), entry("status", 200),
            entry("requestTime", 100L), entry("contentLength", 78L),
            entry("userAgent", userAgent));
    }

    @Test
    public void testDisableTimestamp() {
        includes.remove(AccessAttribute.TIMESTAMP);
        accessJsonLayout.setIncludes(includes);

        assertThat(accessJsonLayout.toJsonMap(event)).containsOnly(
            entry("remoteUser", "john"),
            entry("method", "GET"), entry("uri", uri),
            entry("protocol", "HTTP/1.1"), entry("status", 200),
            entry("requestTime", 100L), entry("contentLength", 78L),
            entry("userAgent", userAgent), entry("remoteAddress", remoteAddress));
    }

    @Test
    public void testEnableSpecificResponseHeader() {
        accessJsonLayout.setResponseHeaders(ImmutableSet.of("transfer-encoding"));

        assertThat(accessJsonLayout.toJsonMap(event)).containsOnly(
            entry("timestamp", timestamp), entry("remoteUser", "john"),
            entry("method", "GET"), entry("uri", uri),
            entry("protocol", "HTTP/1.1"), entry("status", 200),
            entry("requestTime", 100L), entry("contentLength", 78L),
            entry("userAgent", userAgent), entry("remoteAddress", remoteAddress),
            entry("responseHeaders", ImmutableMap.of("Transfer-Encoding", "chunked")));
    }

    @Test
    public void testEnableSpecificRequestHeader() {
        accessJsonLayout.setRequestHeaders(ImmutableSet.of("user-agent"));

        assertThat(accessJsonLayout.toJsonMap(event)).containsOnly(
            entry("timestamp", timestamp), entry("remoteUser", "john"),
            entry("method", "GET"), entry("uri", uri),
            entry("protocol", "HTTP/1.1"), entry("status", 200),
            entry("requestTime", 100L), entry("contentLength", 78L),
            entry("userAgent", userAgent), entry("remoteAddress", remoteAddress),
            entry("headers", ImmutableMap.of("User-Agent", userAgent)));
    }

    @Test
    public void testEnableEverything() {
        accessJsonLayout.setIncludes(EnumSet.allOf(AccessAttribute.class));
        accessJsonLayout.setRequestHeaders(ImmutableSet.of("Host", "User-Agent"));
        accessJsonLayout.setResponseHeaders(ImmutableSet.of("Transfer-Encoding", "Content-Type"));

        assertThat(accessJsonLayout.toJsonMap(event)).containsOnly(
            entry("timestamp", timestamp), entry("remoteUser", "john"),
            entry("method", "GET"), entry("uri", uri),
            entry("protocol", "HTTP/1.1"), entry("status", 200),
            entry("requestTime", 100L), entry("contentLength", 78L),
            entry("userAgent", userAgent), entry("remoteAddress", remoteAddress),
            entry("responseHeaders", responseHeaders),
            entry("responseContent", responseContent),
            entry("port", 8080), entry("requestContent", ""),
            entry("headers", requestHeaders),
            entry("remoteHost", remoteHost), entry("url", url),
            entry("serverName", serverName));
    }

    @Test
    public void testAddAdditionalFields() {
        accessJsonLayout = new AccessJsonLayout(jsonFormatter, timestampFormatter, includes, ImmutableMap.of(),
            ImmutableMap.of("serviceName", "user-service", "serviceVersion", "1.2.3"));
        assertThat(accessJsonLayout.toJsonMap(event)).containsOnly(
            entry("timestamp", timestamp), entry("remoteUser", "john"),
            entry("method", "GET"), entry("uri", uri),
            entry("protocol", "HTTP/1.1"), entry("status", 200),
            entry("requestTime", 100L), entry("contentLength", 78L),
            entry("userAgent", userAgent), entry("remoteAddress", remoteAddress),
            entry("serviceName", "user-service"), entry("serviceVersion", "1.2.3"));
    }

    @Test
    public void testCustomFieldNames() {
        accessJsonLayout = new AccessJsonLayout(jsonFormatter, timestampFormatter, includes,
            ImmutableMap.of("remoteUser", "remote_user", "userAgent", "user_agent",
                "remoteAddress", "remote_address", "contentLength", "content_length",
                "requestTime", "request_time"), ImmutableMap.of());
        assertThat(accessJsonLayout.toJsonMap(event)).containsOnly(
            entry("timestamp", timestamp), entry("remote_user", "john"),
            entry("method", "GET"), entry("uri", uri),
            entry("protocol", "HTTP/1.1"), entry("status", 200),
            entry("request_time", 100L), entry("content_length", 78L),
            entry("user_agent", userAgent), entry("remote_address", remoteAddress));
    }

    @Test
    public void testProducesCorrectJson() throws Exception {
        JsonNode json = objectMapper.readTree(accessJsonLayout.doLayout(event));
        assertThat(json).isNotNull();
        assertThat(json.get("timestamp").asText()).isEqualTo(timestamp);
        assertThat(json.get("remoteUser").asText()).isEqualTo("john");
        assertThat(json.get("method").asText()).isEqualTo("GET");
        assertThat(json.get("uri").asText()).isEqualTo(uri);
        assertThat(json.get("protocol").asText()).isEqualTo("HTTP/1.1");
        assertThat(json.get("status").asInt()).isEqualTo(200);
        assertThat(json.get("requestTime").asInt()).isEqualTo(100);
        assertThat(json.get("contentLength").asInt()).isEqualTo(78);
        assertThat(json.get("userAgent").asText()).isEqualTo(userAgent);
        assertThat(json.get("remoteAddress").asText()).isEqualTo(remoteAddress);
    }
}
