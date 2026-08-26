package com.example.request_log;

import com.example.request_log.helper.RequestLogParser;
import com.example.request_log.helper.RequestLogParser.LogLine;
import ch.qos.logback.access.common.servlet.TeeFilter;
import ch.qos.logback.access.common.spi.IAccessEvent;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.ContextBase;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.request.logging.LogbackAccessRequestLog;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;
import jakarta.servlet.DispatcherType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import org.assertj.core.api.Assertions;
import org.eclipse.jetty.ee10.servlet.SessionHandler;
import org.eclipse.jetty.server.RequestLog;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class LogbackAccessRequestLogIntegrationTest extends AbstractRequestLogIntegrationTest {
    private static final String USER_AGENT = "TestApplication (test-request-logs)";

    private static final ConfigOverride[] COMMON_CONFIG_OVERRIDES = new ConfigOverride[]{
        ConfigOverride.config("server.requestLog.type", "access")};

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class DefaultPattern {
        private final DropwizardTestSupport<Configuration> testSupport = createAppTestSupport(COMMON_CONFIG_OVERRIDES);

        @BeforeAll
        public void setUp() throws Exception {
            testSupport.before();
        }

        @AfterAll
        public void tearDown() {
            testSupport.after();
        }

        @Test
        void unauthenticatedGet() throws Exception {
            // given
            String url = String.format("http://localhost:%d/greet?name=Charley", testSupport.getLocalPort());

            // when
            //    make requests
            for (int i = 0; i < 100; i++) {
                testSupport.getClient()
                    .target(url)
                    .request()
                    .header(HttpHeaders.USER_AGENT, USER_AGENT)
                    .get()
                    .close();
            }
            //   wait for logs to be written
            sendAndAwaitSentinel(testSupport);

            // then
            List<LogLine> parsedLogs = readTestLogLines().stream()
                .map(REQUEST_LOG_PARSER_STOCK_FORMAT::parseLog)
                .toList();
            assertThat(parsedLogs)
                .hasSize(100)
                .allSatisfy(line -> {
                    assertThat(line.getRemoteHost())
                        .isEqualTo(CLIENT_REMOTE_HOST);
                    assertThat(line.getForwardingHost())
                        .isEqualTo("-");
                    assertThat(line.getRemoteUser())
                        .isEqualTo("-");
                    assertThat(line.getTimestamp())
                        .matches(TIMESTAMP_DATA_PATTERN_STOCK_FORMAT);
                    assertThat(line.getMethod())
                        .isEqualTo("GET");
                    assertThat(line.getUri())
                        // %r (logback-access) is the full request line including query
                        .isEqualTo("/greet?name=Charley");
                    assertThat(line.getProtocol())
                        .isEqualTo("HTTP/1.1");
                    assertThat(line.getStatus())
                        .isEqualTo("200");
                    // bytes field sufficiently asserted during parsing
                    assertThat(line.getReferer())
                        .isEqualTo("-");
                    assertThat(line.getUserAgent())
                        .isEqualTo(USER_AGENT);
                    // durationMs field sufficiently asserted during parsing
                });
        }

        @Test
        void authenticatedGets() throws Exception {
            // given
            String username1 = "admin";
            String password1 = "";
            String basicAuth1 = String.format("%s:%s", username1, password1);
            String basicAuthHeader1 = "Basic " + Base64.getEncoder().encodeToString(
                basicAuth1.getBytes(StandardCharsets.UTF_8));
            String username2 = "user2";
            String password2 = "";
            String basicAuth2 = String.format("%s:%s", username2, password2);
            String basicAuthHeader2 = "Basic " + Base64.getEncoder().encodeToString(
                basicAuth2.getBytes(StandardCharsets.UTF_8));
            String url = String.format("http://localhost:%d/greet/authenticated", testSupport.getLocalPort());

            // when
            //    make requests
            testSupport.getClient()
                .target(url)
                .request()
                .header(HttpHeaders.USER_AGENT, USER_AGENT)
                .header(HttpHeaders.AUTHORIZATION, basicAuthHeader1)
                .get()
                .close();
            testSupport.getClient()
                .target(url)
                .request()
                .header(HttpHeaders.USER_AGENT, USER_AGENT)
                .header(HttpHeaders.AUTHORIZATION, basicAuthHeader2)
                .get()
                .close();
            //   wait for logs to be written
            sendAndAwaitSentinel(testSupport);

            // then
            List<LogLine> parsedLogs = readTestLogLines().stream()
                .map(REQUEST_LOG_PARSER_STOCK_FORMAT::parseLog)
                .toList();
            assertThat(parsedLogs)
                .hasSize(2)
                .allSatisfy(line -> {
                    assertThat(line.getRemoteHost())
                        .isEqualTo(CLIENT_REMOTE_HOST);
                    assertThat(line.getForwardingHost())
                        .isEqualTo("-");
                    assertThat(line.getTimestamp())
                        .matches(TIMESTAMP_DATA_PATTERN_STOCK_FORMAT);
                    assertThat(line.getMethod())
                        .isEqualTo("GET");
                    assertThat(line.getUri())
                        .isEqualTo("/greet/authenticated");
                    assertThat(line.getProtocol())
                        .isEqualTo("HTTP/1.1");
                    assertThat(line.getStatus())
                        .isEqualTo("200");
                    // bytes field sufficiently asserted during parsing
                    assertThat(line.getReferer())
                        .isEqualTo("-");
                    assertThat(line.getUserAgent())
                        .isEqualTo(USER_AGENT);
                    // durationMs field sufficiently asserted during parsing
                })
                .satisfies(ignored -> {
                    assertThat(parsedLogs.get(0).getRemoteUser())
                        .isEqualTo("admin");
                    assertThat(parsedLogs.get(1).getRemoteUser())
                        .isEqualTo("user2");
                });
        }

        @Test
        void variousHttpMethods() throws Exception {
            // given
            String greetUrl = String.format("http://localhost:%d/greet?name=Charley", testSupport.getLocalPort());
            String submitUrl = String.format("http://localhost:%d/greet/submit", testSupport.getLocalPort());

            // when
            //    make requests
            testSupport.getClient()
                .target(greetUrl)
                .request()
                .get()
                .close();
            testSupport.getClient()
                .target(greetUrl)
                .request()
                .head()
                .close();
            testSupport.getClient()
                .target(submitUrl)
                .request()
                .post(Entity.entity(new PostParameter("hi"), MediaType.APPLICATION_JSON_TYPE))
                .close();
            //   wait for logs to be written
            sendAndAwaitSentinel(testSupport);

            // then
            List<LogLine> parsedLogs = readTestLogLines().stream()
                .map(REQUEST_LOG_PARSER_STOCK_FORMAT::parseLog)
                .toList();
            assertThat(parsedLogs)
                .hasSize(3)
                .allSatisfy(logLine ->
                    assertThat(logLine.getStatus())
                        .isEqualTo("200"))
                .satisfies(logLines -> {
                    assertThat(logLines.get(0).getMethod())
                        .isEqualTo("GET");
                    assertThat(logLines.get(1).getMethod())
                        .isEqualTo("HEAD");
                    assertThat(logLines.get(2).getMethod())
                        .isEqualTo("POST");
                });
        }

        @Test
        void variousHttpStatuses() throws Exception {
            // given
            String greetUrl = String.format("http://localhost:%d/greet?name=Charley", testSupport.getLocalPort());
            String submitUrl = String.format("http://localhost:%d/greet/submit", testSupport.getLocalPort());
            String invalidUrl = String.format("http://localhost:%d/nonexistant", testSupport.getLocalPort());

            // when
            //    make requests
            testSupport.getClient()
                .target(greetUrl)
                .request()
                .post(null)
                .close();
            testSupport.getClient()
                .target(submitUrl)
                .request()
                .post(Entity.entity("{ }", MediaType.APPLICATION_JSON_TYPE))
                .close();
            testSupport.getClient()
                .target(invalidUrl)
                .request()
                .get()
                .close();
            //   wait for logs to be written
            sendAndAwaitSentinel(testSupport);

            // then
            List<LogLine> parsedLogs = readTestLogLines().stream()
                .map(REQUEST_LOG_PARSER_STOCK_FORMAT::parseLog)
                .toList();
            assertThat(parsedLogs)
                .hasSize(3)
                .satisfies(logLines -> {
                    assertThat(logLines.get(0).getStatus())
                        .isEqualTo("405");
                    assertThat(logLines.get(1).getStatus())
                        .isEqualTo("422");
                    assertThat(logLines.get(2).getStatus())
                        .isEqualTo("404");
                });
        }

        @Test
        void variousUserAgentsAndReferers() throws Exception {
            // given
            String url = String.format("http://localhost:%d/greet?name=Charley", testSupport.getLocalPort());

            // when
            //    make requests
            testSupport.getClient()
                .target(url)
                .request()
                .header("Referer", "https://example5678.com/")
                .header(HttpHeaders.USER_AGENT, "user agent 111")
                .get()
                .close();
            testSupport.getClient()
                .target(url)
                .request()
                .header("Referer", "https://example1234.com/")
                .header(HttpHeaders.USER_AGENT, "user agent 222$#@#*$&-(O@!")
                .get()
                .close();
            //   wait for logs to be written
            sendAndAwaitSentinel(testSupport);

            // then
            List<LogLine> parsedLogs = readTestLogLines().stream()
                .map(REQUEST_LOG_PARSER_STOCK_FORMAT::parseLog)
                .toList();
            assertThat(parsedLogs)
                .hasSize(2)
                .allSatisfy(logLine ->
                    assertThat(logLine.getStatus())
                        .isEqualTo("200"))
                .satisfies(logLines -> {
                    assertThat(logLines.get(0).getReferer())
                        .isEqualTo("https://example5678.com/");
                    assertThat(logLines.get(0).getUserAgent())
                        .isEqualTo("user agent 111");
                    assertThat(logLines.get(1).getReferer())
                        .isEqualTo("https://example1234.com/");
                    assertThat(logLines.get(1).getUserAgent())
                        .isEqualTo("user agent 222$#@#*$&-(O@!");
                });
        }
    }

    // useForwardedHeaders allows us to spoof our source IP and thus test the remoteHost field
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class ForwardedHeadersEnabledApp {
        private final DropwizardTestSupport<Configuration> testSupport = createAppTestSupport(
            Stream.concat(
                Arrays.stream(COMMON_CONFIG_OVERRIDES),
                Stream.of(ConfigOverride.config("server.applicationConnectors[0].useForwardedHeaders", "true"))
            ));

        @BeforeAll
        public void setUp() throws Exception {
            testSupport.before();
        }

        @AfterAll
        public void tearDown() {
            testSupport.after();
        }

        @Test
        void getFromDifferentSourceAddresses() throws Exception {
            // given
            String url = String.format("http://localhost:%d/greet?name=Charley", testSupport.getLocalPort());

            // when
            //    make requests
            testSupport.getClient()
                .target(url)
                .request()
                .header("X-Forwarded-For", "203.0.113.42")
                .header(HttpHeaders.USER_AGENT, USER_AGENT)
                .get()
                .close();
            testSupport.getClient()
                .target(url)
                .request()
                .header("X-Forwarded-For", "198.51.100.42")
                .header(HttpHeaders.USER_AGENT, USER_AGENT)
                .get()
                .close();
            //   wait for logs to be written
            sendAndAwaitSentinel(testSupport);

            // then
            List<LogLine> parsedLogs = readTestLogLines().stream()
                .map(REQUEST_LOG_PARSER_STOCK_FORMAT::parseLog)
                .toList();
            assertThat(parsedLogs)
                .hasSize(2)
                .allSatisfy(line -> {
                    assertThat(line.getForwardingHost())
                        .isEqualTo("-");
                    assertThat(line.getRemoteUser())
                        .isEqualTo("-");
                    assertThat(line.getTimestamp())
                        .matches(TIMESTAMP_DATA_PATTERN_STOCK_FORMAT);
                    assertThat(line.getMethod())
                        .isEqualTo("GET");
                    assertThat(line.getUri())
                        // %r (logback-access) is the full request line including query
                        .isEqualTo("/greet?name=Charley");
                    assertThat(line.getProtocol())
                        .isEqualTo("HTTP/1.1");
                    assertThat(line.getStatus())
                        .isEqualTo("200");
                })
                .satisfies(
                    list -> assertThat(list.get(0).getRemoteHost()).isEqualTo("203.0.113.42"),
                    list -> assertThat(list.get(1).getRemoteHost()).isEqualTo("198.51.100.42"));
        }
    }

    // Postulates a possible deployment scenario where we want to trust the upstream sender's X-Forwarded-For value as
    // the 'client' we log (assuming load balancer strips out any client-supplied value). And where we abuse the
    // 'ident' field for logging "forwarding proxy" (i.e. the LB) IP.
    //
    // Also demonstrates capturing an authenticated user propagated from the LB via X-Forwarded-User: the
    // HeaderToAttributeFilter promotes that header to the 'forwardedClient' request attribute, and the log format
    // reads it via %reqAttribute{forwardedClient}. We route it into the %u slot of the log format so an off-the-shelf
    // access-log parser sees the user in its expected position. This works around the RequestWrapper trap where
    // %u itself always renders '-' regardless of authentication (see REQUEST_LOG_PATTERN_REFERENCE.md).
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class PossibleLoadBalancerReverseProxySetup {
        // useForwardedHeaders is off so we can read the real client IP.
        // client IP moved to 'ident' field.
        // forwarded IP put in regular spot for remote host.
        // %reqAttribute{forwardedClient} (populated by HeaderToAttributeFilter from X-Forwarded-User) put in the %u slot.
        private static final String LOG_FORMAT = "%i{X-Forwarded-For} %a{client} %reqAttribute{forwardedClient} "
            + "[%t{dd/MMM/yyyy:HH:mm:ss Z}] \"%r\" %s %b \"%i{Referer}\" \"%i{User-Agent}\" %D";

        private final DropwizardTestSupport<Configuration> testSupport = createAppTestSupport(
            Stream.concat(
                Arrays.stream(COMMON_CONFIG_OVERRIDES),
                Stream.of(ConfigOverride.config("server.requestLog.appenders[0].logFormat", LOG_FORMAT))
            ));

        @BeforeAll
        public void setUp() throws Exception {
            testSupport.before();
        }

        @AfterAll
        public void tearDown() {
            testSupport.after();
        }

        @Test
        void getFromDifferentSourceAddresses() throws Exception {
            // given
            String url = String.format("http://localhost:%d/greet?name=Charley", testSupport.getLocalPort());

            // when
            //    make requests
            testSupport.getClient()
                .target(url)
                .request()
                .header("X-Forwarded-For", "203.0.113.42")
                .header("X-Forwarded-User", "alice")
                .header(HttpHeaders.USER_AGENT, USER_AGENT)
                .get()
                .close();
            testSupport.getClient()
                .target(url)
                .request()
                .header("X-Forwarded-For", "198.51.100.42")
                .header("X-Forwarded-User", "bob")
                .header(HttpHeaders.USER_AGENT, USER_AGENT)
                .get()
                .close();
            //   wait for logs to be written
            sendAndAwaitSentinel(testSupport);

            // then
            List<LogLine> parsedLogs = readTestLogLines().stream()
                .map(REQUEST_LOG_PARSER_STOCK_FORMAT::parseLog)
                .toList();
            assertThat(parsedLogs)
                .hasSize(2)
                .allSatisfy(line -> {
                    assertThat(line.getForwardingHost())
                        .isEqualTo("127.0.0.1");
                    assertThat(line.getTimestamp())
                        .matches(TIMESTAMP_DATA_PATTERN_STOCK_FORMAT);
                    assertThat(line.getMethod())
                        .isEqualTo("GET");
                    assertThat(line.getUri())
                        // %r (logback-access) is the full request line including query
                        .isEqualTo("/greet?name=Charley");
                    assertThat(line.getProtocol())
                        .isEqualTo("HTTP/1.1");
                    assertThat(line.getStatus())
                        .isEqualTo("200");
                })
                .satisfies(
                    // "remoteHost" and "remoteUser" here are just parser group names - the format actually puts
                    // %i{X-Forwarded-For} in position 1 and %reqAttribute{forwardedClient} in the %u slot.
                    list -> assertThat(list.get(0).getRemoteHost()).isEqualTo("203.0.113.42"),
                    list -> assertThat(list.get(1).getRemoteHost()).isEqualTo("198.51.100.42"),
                    list -> assertThat(list.get(0).getRemoteUser()).isEqualTo("alice"),
                    list -> assertThat(list.get(1).getRemoteUser()).isEqualTo("bob"));
        }
    }

    // Kitchen-sink app: one big pattern capturing many conversion words, tested by many @Test methods that each drive
    // a specific converter with a targeted request. Tests here assert against the inferred *intended* behavior of
    // Logback Access's various request log field keys.
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class KitchenSinkApp {
        private static final String LOG_FORMAT =
              "%h|%a|%A|%localPort|%l|%u|%v|%I|%S|%m|%U|%q|%r|%H|%s|%b|%D|%T"
            + "|%t{dd/MMM/yyyy:HH:mm:ss Z}"
            + "|%i{User-Agent}|%i{Referer}|%i{X-Custom-Header}"
            + "|%responseHeader{Content-Type}|%responseHeader{X-Response-Header}"
            + "|%reqCookie{sessionid}"
            + "|%reqAttribute{traceId}|%reqAttribute{forwardedClient}"
            + "|%reqParameter{name}"
            + "|%requestContent|%responseContent";

        private static final RequestLogParser PARSER = new RequestLogParser(
              "(?<remoteHost>[^|]*)\\|"
            + "(?<remoteIP>[^|]*)\\|"
            + "(?<localIP>[^|]*)\\|"
            + "(?<localPort>[^|]*)\\|"
            + "(?<identd>[^|]*)\\|"
            + "(?<remoteUser>[^|]*)\\|"
            + "(?<serverName>[^|]*)\\|"
            + "(?<threadName>[^|]*)\\|"
            + "(?<sessionID>[^|]*)\\|"
            + "(?<method>[^|]*)\\|"
            + "(?<uriPath>[^|]*)\\|"
            + "(?<queryString>[^|]*)\\|"
            + "(?<requestLine>[^|]*)\\|"
            + "(?<protocol>[^|]*)\\|"
            + "(?<status>\\d{3})\\|"
            + "(?<bytes>\\d+|-)\\|"
            + "(?<durationMs>-?\\d+)\\|"
            + "(?<durationSec>-?\\d+)\\|"
            + "(?<timestamp>[^|]*)\\|"
            + "(?<userAgent>[^|]*)\\|"
            + "(?<referer>[^|]*)\\|"
            + "(?<customReqHeader>[^|]*)\\|"
            + "(?<contentType>[^|]*)\\|"
            + "(?<customRespHeader>[^|]*)\\|"
            + "(?<sessionCookie>[^|]*)\\|"
            + "(?<traceId>[^|]*)\\|"
            + "(?<forwardedClient>[^|]*)\\|"
            + "(?<nameParam>[^|]*)\\|"
            + "(?<requestContent>[^|]*)\\|"
            + "(?<responseContent>[^|]*)");

        private final DropwizardTestSupport<Configuration> testSupport = createAppTestSupport(
            Stream.concat(
                Arrays.stream(COMMON_CONFIG_OVERRIDES),
                Stream.of(ConfigOverride.config("server.requestLog.appenders[0].logFormat", LOG_FORMAT))
            ));

        @BeforeAll
        public void setUp() throws Exception {
            testSupport.before();
        }

        @AfterAll
        public void tearDown() {
            testSupport.after();
        }

        // --- helpers ---

        private String baseUrl() {
            return String.format("http://localhost:%d", testSupport.getLocalPort());
        }

        // GET /greet?name=Charley - the baseline request used by most tests. Returns one parsed log line.
        private LogLine makeDefaultRequest() throws IOException {
            testSupport.getClient()
                .target(baseUrl() + "/greet?name=Charley")
                .request()
                .header(HttpHeaders.USER_AGENT, "kitchen-sink-agent")
                .get()
                .close();
            sendAndAwaitSentinel(testSupport);
            List<LogLine> lines = readTestLogLines().stream()
                .map(PARSER::parseLog)
                .toList();
            assertThat(lines)
                .hasSize(1);
            return lines.get(0);
        }

        // --- Identity / network ---

        @Test
        void remoteHost_isLoopback() throws Exception {
            // when
            LogLine resultingLogLine = makeDefaultRequest();

            // then
            assertThat(resultingLogLine.get("remoteHost"))
                .isEqualTo(CLIENT_REMOTE_HOST);
        }

        @Test
        void remoteIP_isLoopback() throws Exception {
            // when
            LogLine resultingLogLine = makeDefaultRequest();

            // then
            //    %a and %h both delegate to the wrapper's getRemoteAddr() - identical output.
            assertThat(resultingLogLine.get("remoteIP"))
                .isEqualTo(CLIENT_REMOTE_HOST);
        }

        @Test
        void localIP_isSomeIpv4Address() throws Exception {
            // when
            LogLine resultingLogLine = makeDefaultRequest();

            // then
            //    InetAddress.getLocalHost() - value varies per host, so shape-only assertion.
            assertThat(resultingLogLine.get("localIP"))
                .matches("\\d+\\.\\d+\\.\\d+\\.\\d+");
        }

        @Test
        void localPort_matchesBoundPort() throws Exception {
            // when
            LogLine resultingLogLine = makeDefaultRequest();

            // then
            assertThat(resultingLogLine.get("localPort"))
                .isEqualTo(String.valueOf(testSupport.getLocalPort()));
        }

        @Test
        void identd_isDash() throws Exception {
            // when
            LogLine resultingLogLine = makeDefaultRequest();

            // then
            assertThat(resultingLogLine.get("identd"))
                .isEqualTo("-");
        }

        @Test
        void serverName_isLocalhost() throws Exception {
            // when
            LogLine resultingLogLine = makeDefaultRequest();

            // then
            assertThat(resultingLogLine.get("serverName"))
                .isEqualTo("localhost");
        }

        // --- Request line ---

        @Test
        void method_matchesRequest() throws Exception {
            // when
            LogLine resultingLogLine = makeDefaultRequest();

            // then
            assertThat(resultingLogLine.get("method"))
                .isEqualTo("GET");
        }

        @Test
        void uriPath_isPathOnly() throws Exception {
            // when
            LogLine resultingLogLine = makeDefaultRequest();

            // then
            assertThat(resultingLogLine.get("uriPath"))
                .isEqualTo("/greet");
        }

        @Test
        void queryString_includesLeadingQuestionMark() throws Exception {
            // when
            LogLine resultingLogLine = makeDefaultRequest();

            // then
            assertThat(resultingLogLine.get("queryString"))
                .isEqualTo("?name=Charley");
        }

        @Test
        void queryString_isEmptyWhenAbsent() throws Exception {
            // when
            //    hit an endpoint with no query string
            testSupport.getClient()
                .target(baseUrl() + "/greet")
                .request()
                .get()
                .close();
            sendAndAwaitSentinel(testSupport);

            // then
            //    %q renders empty when there's no query string (not "-"; the "?" prefix is dropped)
            List<LogLine> lines = readTestLogLines().stream()
                .map(PARSER::parseLog)
                .toList();
            assertThat(lines)
                .singleElement()
                .satisfies(line ->
                    assertThat(line.get("queryString")).isEmpty());
        }

        @Test
        void requestLine_isFull() throws Exception {
            // when
            LogLine resultingLogLine = makeDefaultRequest();

            // then
            assertThat(resultingLogLine.get("requestLine"))
                .isEqualTo("GET /greet?name=Charley HTTP/1.1");
        }

        @Test
        void protocol_isHttp11() throws Exception {
            // when
            LogLine resultingLogLine = makeDefaultRequest();

            // then
            assertThat(resultingLogLine.get("protocol"))
                .isEqualTo("HTTP/1.1");
        }

        // --- Response ---

        @Test
        void status_matchesResponse() throws Exception {
            // when
            testSupport.getClient()
                .target(baseUrl() + "/greet/echo?status=204&body=")
                .request()
                .get()
                .close();
            sendAndAwaitSentinel(testSupport);

            // then
            List<LogLine> lines = readTestLogLines().stream()
                .map(PARSER::parseLog)
                .toList();
            assertThat(lines)
                .singleElement()
                .satisfies(line ->
                    assertThat(line.get("status"))
                        .isEqualTo("204"));
        }

        @Test
        void bytes_matchesResponseBodyLength() throws Exception {
            // when
            testSupport.getClient()
                .target(baseUrl() + "/greet/echo?body=hello")
                .request()
                .get()
                .close();
            sendAndAwaitSentinel(testSupport);

            // then
            List<LogLine> lines = readTestLogLines().stream()
                .map(PARSER::parseLog)
                .toList();
            assertThat(lines)
                .singleElement()
                .satisfies(line ->
                    assertThat(line.get("bytes"))
                        .isEqualTo("5"));
        }

        @Test
        void bytes_isZeroForEmptyResponseBody() throws Exception {
            // when
            //    request an empty response body
            testSupport.getClient()
                .target(baseUrl() + "/greet/echo?body=")
                .request()
                .get()
                .close();
            sendAndAwaitSentinel(testSupport);

            // then
            //    logback-access %b renders "0" (not "-") for an empty body. Apache HTTPD's %b renders "-" in this case
            //    but logback-access maps both %b and %B to the same converter, which prints the numeric count.
            //    Only the SENTINEL value (-1, "unknown") maps to "-".
            List<LogLine> lines = readTestLogLines().stream()
                .map(PARSER::parseLog)
                .toList();
            assertThat(lines)
                .singleElement()
                .satisfies(line ->
                    assertThat(line.get("bytes"))
                        .isEqualTo("0"));
        }

        // --- Timing ---

        @Test
        void timestamp_isWellFormed() throws Exception {
            // when
            LogLine resultingLogLine = makeDefaultRequest();

            // then
            assertThat(resultingLogLine.get("timestamp"))
                .matches(TIMESTAMP_DATA_PATTERN_STOCK_FORMAT);
        }

        @Test
        void durations_areNonNegativeIntegers() throws Exception {
            // when
            LogLine line = makeDefaultRequest();

            // then
            assertThat(line.get("durationMs"))
                .matches("\\d+");
            assertThat(line.get("durationSec"))
                .matches("\\d+");
            assertThat(Long.parseLong(line.get("durationMs")))
                .isGreaterThanOrEqualTo(0);
            assertThat(Long.parseLong(line.get("durationSec")))
                .isGreaterThanOrEqualTo(0);
        }

        // --- Request headers ---

        @Test
        void userAgent_isFromRequest() throws Exception {
            // when
            LogLine resultingLogLine = makeDefaultRequest();

            // then
            assertThat(resultingLogLine.get("userAgent"))
                .isEqualTo("kitchen-sink-agent");
        }

        @Test
        void userAgent_isDashWhenMissing() throws Exception {
            // when
            try (Socket clientSocket = new Socket("localhost", testSupport.getLocalPort())) {
                // bypass HTTP client restrictions where they force a user agent to be supplied
                PrintWriter socketPrinter = new PrintWriter(clientSocket.getOutputStream());
                socketPrinter.println("GET /greet?name=Charley HTTP/1.1");
                socketPrinter.println("Host: localhost");
                socketPrinter.println("Connection: close");
                socketPrinter.println();
                socketPrinter.flush();
                // wait for response and socket close
                clientSocket.getInputStream().readAllBytes();
            }
            sendAndAwaitSentinel(testSupport);

            // then
            //    Missing request header renders "-".
            List<LogLine> lines = readTestLogLines().stream()
                .map(PARSER::parseLog)
                .toList();
            assertThat(lines)
                .singleElement()
                .satisfies(line ->
                    assertThat(line.get("userAgent"))
                        .isEqualTo("-"));
        }

        @Test
        void referer_isDashWhenMissing() throws Exception {
            // when
            LogLine resultingLogLine = makeDefaultRequest();

            // then
            //    logback-access %i{HeaderName} renders "-" when the header is absent.
            assertThat(resultingLogLine.get("referer")).isEqualTo("-");
        }

        @Test
        void referer_isRenderedFromRequest() throws Exception {
            // when
            testSupport.getClient()
                .target(baseUrl() + "/greet?name=Charley")
                .request()
                .header("Referer", "https://example.com/prev")
                .get()
                .close();
            sendAndAwaitSentinel(testSupport);

            // then
            List<LogLine> lines = readTestLogLines().stream()
                .map(PARSER::parseLog)
                .toList();
            assertThat(lines)
                .singleElement()
                .satisfies(line ->
                    assertThat(line.get("referer"))
                        .isEqualTo("https://example.com/prev"));
        }

        @Test
        void customRequestHeader_isRendered() throws Exception {
            // when
            testSupport.getClient()
                .target(baseUrl() + "/greet?name=Charley")
                .request()
                .header(HttpHeaders.USER_AGENT, "kitchen-sink-agent")
                .header("X-Custom-Header", "custom-value-xyz")
                .get()
                .close();
            sendAndAwaitSentinel(testSupport);

            // then
            List<LogLine> lines = readTestLogLines().stream()
                .map(PARSER::parseLog)
                .toList();
            assertThat(lines)
                .singleElement()
                .satisfies(line ->
                    assertThat(line.get("customReqHeader"))
                        .isEqualTo("custom-value-xyz"));
        }

        @Test
        void customRequestHeader_isDashWhenMissing() throws Exception {
            // when
            //    makeDefaultRequest() doesn't send X-Custom-Header.
            LogLine resultingLogLine = makeDefaultRequest();

            // then
            assertThat(resultingLogLine.get("customReqHeader"))
                .isEqualTo("-");
        }

        // --- Response headers ---

        @Test
        void responseHeader_contentType_isFromJerseyResponse() throws Exception {
            // when
            LogLine resultingLogLine = makeDefaultRequest();

            // then
            assertThat(resultingLogLine.get("contentType"))
                .isEqualTo("text/plain");
        }

        @Test
        void responseHeader_custom_isRenderedFromEcho() throws Exception {
            // when
            testSupport.getClient()
                .target(baseUrl() + "/greet/echo?respHeader=X-Response-Header:server-value")
                .request()
                .get()
                .close();
            sendAndAwaitSentinel(testSupport);

            // then
            List<LogLine> lines = readTestLogLines().stream()
                .map(PARSER::parseLog)
                .toList();
            assertThat(lines)
                .singleElement()
                .satisfies(line ->
                    assertThat(line.get("customRespHeader"))
                        .isEqualTo("server-value"));
        }

        @Test
        void responseHeader_isDashWhenMissing() throws Exception {
            // when
            //    Make a request that doesn't set X-Response-Header (baseline /greet endpoint).
            LogLine resultingLogLine = makeDefaultRequest();

            // then
            assertThat(resultingLogLine.get("customRespHeader"))
                .isEqualTo("-");
        }

        // --- Cookies ---

        @Test
        void sessionCookie_isDashWhenMissing() throws Exception {
            // when
            LogLine resultingLogLine = makeDefaultRequest();

            // then
            assertThat(resultingLogLine.get("sessionCookie"))
                .isEqualTo("-");
        }

        @Test
        void sessionCookie_isRenderedFromRequest() throws Exception {
            // when
            testSupport.getClient()
                .target(baseUrl() + "/greet?name=Charley")
                .request()
                .header(HttpHeaders.USER_AGENT, "kitchen-sink-agent")
                .header("Cookie", "sessionid=abc123")
                .get()
                .close();
            sendAndAwaitSentinel(testSupport);

            // then
            List<LogLine> lines = readTestLogLines().stream()
                .map(PARSER::parseLog)
                .toList();
            assertThat(lines)
                .singleElement()
                .satisfies(line ->
                    assertThat(line.get("sessionCookie"))
                        .isEqualTo("abc123"));
        }

        // --- Request attributes (populated by HeaderToAttributeFilter) ---

        @Test
        void traceId_isPopulatedFromHeader() throws Exception {
            // when
            testSupport.getClient()
                .target(baseUrl() + "/greet?name=Charley")
                .request()
                .header(HttpHeaders.USER_AGENT, "kitchen-sink-agent")
                .header("X-Trace-Id", "trace-xyz")
                .get()
                .close();
            sendAndAwaitSentinel(testSupport);

            // then
            List<LogLine> lines = readTestLogLines().stream()
                .map(PARSER::parseLog)
                .toList();
            assertThat(lines)
                .singleElement()
                .satisfies(line ->
                    assertThat(line.get("traceId"))
                        .isEqualTo("trace-xyz"));
        }

        @Test
        void forwardedClient_isPopulatedFromHeader() throws Exception {
            // when
            testSupport.getClient()
                .target(baseUrl() + "/greet?name=Charley")
                .request()
                .header(HttpHeaders.USER_AGENT, "kitchen-sink-agent")
                .header("X-Forwarded-User", "alice")
                .get()
                .close();
            sendAndAwaitSentinel(testSupport);

            // then
            List<LogLine> lines = readTestLogLines().stream()
                .map(PARSER::parseLog)
                .toList();
            assertThat(lines)
                .singleElement()
                .satisfies(line ->
                    assertThat(line.get("forwardedClient"))
                        .isEqualTo("alice"));
        }

        @Test
        void traceId_isDashWhenHeaderMissing() throws Exception {
            // when
            //    default request doesn't send X-Trace-Id, so HeaderToAttributeFilter doesn't set the attribute
            LogLine resultingLogLine = makeDefaultRequest();

            // then
            //    %reqAttribute{traceId} for an unset attribute renders "-".
            assertThat(resultingLogLine.get("traceId"))
                .isEqualTo("-");
        }

        @Test
        void forwardedClient_isDashWhenHeaderMissing() throws Exception {
            // when
            //    default request doesn't send X-Forwarded-User, so HeaderToAttributeFilter doesn't set the attribute
            LogLine resultingLogLine = makeDefaultRequest();

            // then
            //    %reqAttribute{forwardedClient} for an unset attribute renders "-".
            assertThat(resultingLogLine.get("forwardedClient"))
                .isEqualTo("-");
        }

        // --- Request parameters ---

        @Test
        void reqParameter_isFromQueryString() throws Exception {
            // when
            LogLine resultingLogLine = makeDefaultRequest();

            // then
            assertThat(resultingLogLine.get("nameParam"))
                .isEqualTo("Charley");
        }

        @Test
        void reqParameter_isDashWhenAbsent() throws Exception {
            // when
            testSupport.getClient()
                .target(baseUrl() + "/greet")
                .request()
                .header(HttpHeaders.USER_AGENT, "kitchen-sink-agent")
                .get()
                .close();
            sendAndAwaitSentinel(testSupport);

            // then
            List<LogLine> lines = readTestLogLines().stream()
                .map(PARSER::parseLog)
                .toList();
            assertThat(lines)
                .singleElement()
                .satisfies(line ->
                    assertThat(line.get("nameParam"))
                        .isEqualTo("-"));
        }

        // --- Bodies (TeeFilter not installed here; both should be empty) ---

        @Test
        void requestAndResponseContents_areEmptyWithoutTeeFilter() throws Exception {
            // when
            LogLine line = makeDefaultRequest();

            // then
            assertThat(line.get("requestContent"))
                .isEmpty();
            assertThat(line.get("responseContent"))
                .isEmpty();
        }

        // --- Session ---

        @Test
        void sessionID_isDashWhenNoSession() throws Exception {
            // when
            LogLine resultingLogLine = makeDefaultRequest();

            // then
            //    WrappedHttpRequest.getSessionID() returns NA ("-") when the underlying Jetty request has no session.
            //    (Positive case for %S with a real session lives in the SessionEnabledApp nested class, which uses
            //    a specialized TestApplication that installs a SessionHandler.)
            assertThat(resultingLogLine.get("sessionID"))
                .isEqualTo("-");
        }

        // --- Thread ---

        @Test
        void threadName_isDropwizardRequestWorkerThread() throws Exception {
            // when
            LogLine resultingLogLine = makeDefaultRequest();

            // then
            //    Dropwizard's request thread pool names threads "dw-<number>".
            assertThat(resultingLogLine.get("threadName"))
                .matches("dw-\\d+");
        }

        // --- Wrapper-trap cases: assert INTENDED behavior; these fail red until the wrapper is fixed ---

        @Test
        void remoteUser_isDashWhenNotAuthenticated() throws Exception {
            // when
            //    default request goes through /greet with no auth header
            LogLine resultingLogLine = makeDefaultRequest();

            // then
            //    Unauthenticated request: %u renders "-".
            assertThat(resultingLogLine.get("remoteUser"))
                .isEqualTo("-");
        }

        @Test
        void remoteUser_reflectsAuthenticatedUser() throws Exception {
            // given
            String basicAuth = "admin:";
            String basicAuthHeader
                = "Basic " + Base64.getEncoder().encodeToString(basicAuth.getBytes(StandardCharsets.UTF_8));

            // when
            testSupport.getClient()
                .target(baseUrl() + "/greet/authenticated")
                .request()
                .header(HttpHeaders.USER_AGENT, "kitchen-sink-agent")
                .header(HttpHeaders.AUTHORIZATION, basicAuthHeader)
                .get()
                .close();
            sendAndAwaitSentinel(testSupport);

            // then
            List<LogLine> lines = readTestLogLines().stream()
                .map(PARSER::parseLog)
                .toList();
            assertThat(lines)
                .singleElement()
                .satisfies(line ->
                    assertThat(line.get("remoteUser"))
                        .isEqualTo("admin"));
        }
    }

    // Keyed converters (%responseHeader, %reqCookie, %reqAttribute, %reqParameter) each render a specific sentinel
    // literal when used without a key. These literals are part of the observable contract.
    //
    // %i (bare) is intentionally not tested: it takes a different path that dumps the entire header map.
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class MissingKeySentinels {
        // Pattern must include %U so the sentinel-request URI (which carries the marker) appears in
        // the log line; otherwise awaitSentinel can never find its marker in the file.
        private static final String LOG_FORMAT = "%U|%responseHeader|%reqCookie|%reqAttribute|%reqParameter";

        private static final RequestLogParser PARSER = new RequestLogParser(
              "(?<uriPath>[^|]*)\\|"
            + "(?<responseHeader>[^|]*)\\|"
            + "(?<reqCookie>[^|]*)\\|"
            + "(?<reqAttribute>[^|]*)\\|"
            + "(?<reqParameter>[^|]*)");

        private final DropwizardTestSupport<Configuration> testSupport = createAppTestSupport(
            Stream.concat(
                Arrays.stream(COMMON_CONFIG_OVERRIDES),
                Stream.of(ConfigOverride.config("server.requestLog.appenders[0].logFormat", LOG_FORMAT))
            ));

        @BeforeAll
        public void setUp() throws Exception {
            testSupport.before();
        }

        @AfterAll
        public void tearDown() {
            testSupport.after();
        }

        @Test
        void keyedConvertersWithoutKeyRenderTheirSentinels() throws Exception {
            // given
            String uri = String.format("http://localhost:%d/greet?name=Charley", testSupport.getLocalPort());

            // when
            testSupport.getClient()
                .target(uri)
                .request()
                .get()
                .close();
            sendAndAwaitSentinel(testSupport);

            // then
            List<LogLine> lines = readTestLogLines().stream()
                .map(PARSER::parseLog)
                .toList();
            assertThat(lines)
                .singleElement()
                    .satisfies(line -> {
                        // Note the upstream typo in INACTIVE_REPONSE_HEADER_CONV (missing 'S')
                        assertThat(line.get("responseHeader"))
                            .isEqualTo("INACTIVE_REPONSE_HEADER_CONV");
                        assertThat(line.get("reqCookie"))
                            .isEqualTo("INACTIVE_COOKIE_CONVERTER");
                        assertThat(line.get("reqAttribute"))
                            .isEqualTo("INACTIVE_REQUEST_ATTRIB_CONV");
                        assertThat(line.get("reqParameter"))
                            .isEqualTo("INACTIVE_REQUEST_PARAM_CONV");
                    });
        }
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class NamedShortcut {
        // logback-access PatternLayout accepts three named shortcuts (case-insensitive) that expand at start time into
        // pre-established common patterns:
        //   common / clf  -> %h %l %u [%t] "%r" %s %b
        //   combined      -> %h %l %u [%t] "%r" %s %b "%i{Referer}" "%i{User-Agent}"
        // Each test below sets logFormat to one of these names and asserts the expanded pattern is what the log line
        // matches.
        @TestInstance(TestInstance.Lifecycle.PER_CLASS)
        private abstract class AbstractNamedShortcut {
            protected final DropwizardTestSupport<Configuration> testSupport;
            private final Pattern expectedLogPattern;

            public AbstractNamedShortcut(String logFormat, Pattern expectedLogPattern) {
                testSupport = createAppTestSupport(
                    Stream.concat(
                        Arrays.stream(COMMON_CONFIG_OVERRIDES),
                        Stream.of(ConfigOverride.config("server.requestLog.appenders[0].logFormat", logFormat))
                    ));
                this.expectedLogPattern = expectedLogPattern;
            }

            @BeforeAll
            public void setUp() throws Exception {
                testSupport.before();
            }

            @AfterAll
            public void tearDown() {
                testSupport.after();
            }

            @Test
            void namedShortcutRendersCorrectEffectivePattern() throws Exception {
                // given
                String uri = String.format("http://localhost:%d/greet?name=Charley", testSupport.getLocalPort());

                // when
                testSupport.getClient()
                    .target(uri)
                    .request()
                    .header(HttpHeaders.USER_AGENT, USER_AGENT)
                    .header("Referer", "https://example.com/")
                    .get()
                    .close();
                sendAndAwaitSentinel(testSupport);

                // then
                List<String> lines = readTestLogLines();
                assertThat(lines)
                    .singleElement()
                    .satisfies(line ->
                        assertThat(lines.get(0))
                            .matches(expectedLogPattern));
            }
        }

        @TestInstance(TestInstance.Lifecycle.PER_CLASS)
        @Nested
        class NamedShortcutCommon extends AbstractNamedShortcut {
            public NamedShortcutCommon() {
                super(
                    "common",
                    // Expanded: %h %l %u [%t] "%r" %s %b
                    Pattern.compile("127\\.0\\.0\\.1 - - \\[[^]]+] \"GET /greet\\?name=Charley HTTP/1\\.1\" 200 \\d+")
                );
            }
        }

        @TestInstance(TestInstance.Lifecycle.PER_CLASS)
        @Nested
        class NamedShortcutClf extends AbstractNamedShortcut {
            public NamedShortcutClf() {
                super(
                    // Uppercase to exercise case-insensitive matching in PatternLayout.start()
                    "CLF",
                    Pattern.compile("127\\.0\\.0\\.1 - - \\[[^]]+] \"GET /greet\\?name=Charley HTTP/1\\.1\" 200 \\d+")
                );
            }
        }

        @TestInstance(TestInstance.Lifecycle.PER_CLASS)
        @Nested
        class NamedShortcutCombined extends AbstractNamedShortcut {
            public NamedShortcutCombined() {
                super(
                    // Mixed case to further exercise case-insensitive matching
                    "Combined",
                    // Expanded: %h %l %u [%t] "%r" %s %b "%i{Referer}" "%i{User-Agent}"
                    Pattern.compile("127\\.0\\.0\\.1 - - \\[[^]]+] \"GET /greet\\?name=Charley HTTP/1\\.1\" 200 \\d+ "
                        + "\"https://example\\.com/\" \"" + Pattern.quote(USER_AGENT) + "\"")
                );
            }
        }
    }

    // Dropwizard replaces the literal %dwTimeZone in the log format with the appender's timeZone.getID()
    // in AbstractAppenderFactory.buildLayout() (via plain String.replace) before logback parses the pattern.
    // With appender timeZone: America/Los_Angeles and %t{yyyy-MM-dd HH:mm:ss Z,%dwTimeZone}, the emitted timestamp
    // should carry the LA offset (-0800 or -0700), not the JVM default zone (which the surefire argLine has pinned
    // to UTC).
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class DwTimeZoneSubstitution {
        private static final String LOG_FORMAT = "%U %t{yyyy-MM-dd HH:mm:ss Z,%dwTimeZone}";

        private final DropwizardTestSupport<Configuration> testSupport = createAppTestSupport(
            Stream.concat(
                Arrays.stream(COMMON_CONFIG_OVERRIDES),
                Stream.of(
                    ConfigOverride.config("server.requestLog.appenders[0].logFormat", LOG_FORMAT),
                    ConfigOverride.config("server.requestLog.appenders[0].timeZone", "America/Los_Angeles"))
            ));

        @BeforeAll
        public void setUp() throws Exception {
            testSupport.before();
        }

        @AfterAll
        public void tearDown() {
            testSupport.after();
        }

        @Test
        void dwTimeZoneIsSubstitutedIntoTimestamp() throws Exception {
            String uri = String.format("http://localhost:%d/greet?name=Charley", testSupport.getLocalPort());

            // when
            testSupport.getClient()
                .target(uri)
                .request()
                .get()
                .close();
            sendAndAwaitSentinel(testSupport);

            // then
            List<String> lines = readTestLogLines();
            assertThat(lines)
                .singleElement()
                .satisfies(line ->
                    // Log line shape: "/greet 2026-08-28 07:03:29 -0700"
                    // The offset is what proves %dwTimeZone was substituted with LA and the timestamp is being
                    // rendered in that zone (not UTC).
                    assertThat(line)
                        .matches("/greet \\d{4}-\\d{2}-\\d{2} \\d{2}:\\d{2}:\\d{2} -0[78]00"));
        }
    }

    // Color composite converters (%red, %green, %boldBlue, ...) wrap their inner content in ANSI escape sequences.
    // The tests below assert on the exact escape bytes rather than "there are some escapes" because parsers-of-logs
    // (grafana loki, elk parsers, etc.) may depend on the exact codes.
    //
    // Shape of a %red(x) rendering: ESC[31m x ESC[0;39m
    //   - ESC[31m    -> set foreground red
    //   - x          -> inner content
    //   - ESC[0;39m  -> reset attributes + default foreground
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Colors {
        // ANSI escape (ESC) is \033 == 0x1B == . We use the Java string literal form here.
        private static final String ESC = "";
        private static final String LOG_FORMAT = "%U|%red(%s)|%boldBlue(%m)";

        private final DropwizardTestSupport<Configuration> testSupport = createAppTestSupport(
            Stream.concat(
                Arrays.stream(COMMON_CONFIG_OVERRIDES),
                Stream.of(ConfigOverride.config("server.requestLog.appenders[0].logFormat", LOG_FORMAT))
            ));

        @BeforeAll
        public void setUp() throws Exception {
            testSupport.before();
        }

        @AfterAll
        public void tearDown() {
            testSupport.after();
        }

        @Test
        void colorConvertersEmitAnsiEscapes() throws Exception {
            // given
            String uri = String.format("http://localhost:%d/greet?name=Charley", testSupport.getLocalPort());

            // when
            testSupport.getClient()
                .target(uri)
                .request()
                .get()
                .close();
            sendAndAwaitSentinel(testSupport);

            // then
            List<String> lines = readTestLogLines();
            assertThat(lines)
                .singleElement()
                .satisfies(line ->
                    // %red(%s)      -> ESC[31m 200 ESC[0;39m
                    // %boldBlue(%m) -> ESC[1;34m GET ESC[0;39m
                    assertThat(line)
                        .isEqualTo("/greet|" + ESC + "[31m200" + ESC + "[0;39m|"
                            + ESC + "[1;34mGET" + ESC + "[0;39m"));
        }
    }

    // Composite converters wrap inner patterns:
    //   %replace(<inner>){<regex>}{<replacement>}  - regex replace over inner's rendered output
    //   %N(<inner1> <inner2> ...)                  - bare grouping; a format modifier applies to the group as a whole
    // Both are inherited from logback-core (Parser.DEFAULT_COMPOSITE_CONVERTER_MAP) and copied into access's converter
    // supplier map by PatternLayout's static initializer.
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class Composites {
        // Two log-line fields separated by `|`:
        //   1. Plain %U                            -> baseline
        //   2. %replace(%U){'greet','REDACTED'}    -> regex replace over the URL path
        //   3. %20(%m %s)                          -> bare grouping around "%m %s", left-padded to 20 chars
        //
        // Note that %replace's options are comma-separated inside a single {...} block.
        private static final String LOG_FORMAT = "%U|%replace(%U){'greet','REDACTED'}|%20(%m %s)";

        private final DropwizardTestSupport<Configuration> testSupport = createAppTestSupport(
            Stream.concat(
                Arrays.stream(COMMON_CONFIG_OVERRIDES),
                Stream.of(ConfigOverride.config("server.requestLog.appenders[0].logFormat", LOG_FORMAT))
            ));

        @BeforeAll
        public void setUp() throws Exception {
            testSupport.before();
        }

        @AfterAll
        public void tearDown() {
            testSupport.after();
        }

        @Test
        void compositesReplaceAndGroupAreApplied() throws Exception {
            // given
            String uri = String.format("http://localhost:%d/greet?name=Charley", testSupport.getLocalPort());

            // when
            testSupport.getClient()
                .target(uri)
                .request()
                .get()
                .close();
            sendAndAwaitSentinel(testSupport);

            // then
            List<String> lines = readTestLogLines();
            assertThat(lines)
                .singleElement()
                .satisfies(line ->
                    // Expected shape:
                    //   %U                               -> "/greet"
                    //   %replace(%U){'greet','REDACTED'} -> "/REDACTED"
                    //   %20(%m %s)                       -> "             GET 200"  (13 spaces + "GET 200" = 20 chars)
                    assertThat(lines.get(0))
                        .isEqualTo("/greet|/REDACTED|             GET 200"));
        }
    }

    // Format modifiers apply to any converter. Six documented forms; %N(...) grouping is already covered in the
    // Composites test above. This nested class covers the remaining five:
    //   %-Nword    -> min width N, right-padded (left-justified)
    //   %Nword     -> min width N, left-padded (right-justified)
    //   %.Mword    -> max width M, truncate the start (keep tail)
    //   %.-Mword   -> max width M, truncate the end (keep head)
    //   %-N.Mword  -> combined; logback's FormattingConverter branches truncate OR pad, never both. So
    //                 %-N.M behaves as "truncate to M if longer than M, else pad to N if shorter than N."
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class FormatModifiers {
        // Fields:
        //   1. %U           -> "/greet"                 baseline / sentinel carrier
        //   2. %-10m        -> "GET       "             min 10, left-justified (7 trailing spaces)
        //   3. %10m         -> "       GET"             min 10, right-justified (7 leading spaces)
        //   4. %.5h         -> "0.0.1"                  max 5, keep tail of "127.0.0.1"
        //   5. %.-5h        -> "127.0"                  max 5, keep head of "127.0.0.1"
        //   6. %-15.20h     -> "127.0.0.1      "        max 20 (no truncate), min 15 left-justified
        private static final String LOG_FORMAT = "%U|%-10m|%10m|%.5h|%.-5h|%-15.20h";

        private final DropwizardTestSupport<Configuration> testSupport = createAppTestSupport(
            Stream.concat(
                Arrays.stream(COMMON_CONFIG_OVERRIDES),
                Stream.of(ConfigOverride.config("server.requestLog.appenders[0].logFormat", LOG_FORMAT))
            ));

        @BeforeAll
        public void setUp() throws Exception {
            testSupport.before();
        }

        @AfterAll
        public void tearDown() {
            testSupport.after();
        }

        @Test
        void formatModifiersPadAndTruncateAsDocumented() throws Exception {
            // given
            String uri = String.format("http://localhost:%d/greet?name=Charley", testSupport.getLocalPort());

            // when
            testSupport.getClient()
                .target(uri)
                .request()
                .get()
                .close();
            sendAndAwaitSentinel(testSupport);

            // then
            List<String> lines = readTestLogLines();
            assertThat(lines)
                .singleElement()
                .satisfies(line ->
                    assertThat(lines.get(0))
                        .isEqualTo("/greet|GET       |       GET|0.0.1|127.0|127.0.0.1      "));
        }
    }

    // Header handling nuances:
    //   1. Multivalued request headers sent as duplicate lines
    //   2. Multivalued request headers sent as one comma-separated line
    //   3. Multivalued *response* headers set via multiple Response.header(...) calls
    //         (resulting in multiple header lines in the response message)
    //   4. Multivalued *response* headers set via single common-separate Response.header(...) call
    //   5. Case-insensitive request-header lookup (%i{User-Agent} == %i{user-agent})
    //   6. Case-insensitive response-header lookup (%responseHeader{Content-Type} == %responseHeader{content-type})
    //
    // Dropwizard's LogbackAccessRequestLog#buildHeaderMap joins multivalued headers with `,`. Upstream logback-access
    // drops all but the last (see https://github.com/qos-ch/logback-access/pull/23).
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class HeaderVariants {
        // Log format captures:
        //   1. %U                                  sentinel carrier
        //   2. %i{X-Foo}                           for request-side multi-value tests
        //   3. %i{User-Agent}                      case-insensitivity request-side (canonical)
        //   4. %i{user-agent}                      case-insensitivity request-side (lowercase)
        //   5. %responseHeader{X-Bar}              for response-side multi-value tests
        //   6. %responseHeader{Content-Type}       case-insensitivity response-side (canonical)
        //   7. %responseHeader{content-type}       case-insensitivity response-side (lowercase)
        private static final String LOG_FORMAT = "%U|%i{X-Foo}|%i{User-Agent}|%i{user-agent}|%responseHeader{X-Bar}"
            + "|%responseHeader{Content-Type}|%responseHeader{content-type}";

        private static final RequestLogParser PARSER = new RequestLogParser(
              "(?<uri>[^|]*)\\|"
            + "(?<xFoo>[^|]*)\\|"
            + "(?<userAgentCanonical>[^|]*)\\|"
            + "(?<userAgentLower>[^|]*)\\|"
            + "(?<xBar>[^|]*)\\|"
            + "(?<contentTypeCanonical>[^|]*)\\|"
            + "(?<contentTypeLower>[^|]*)");

        private final DropwizardTestSupport<Configuration> testSupport = createAppTestSupport(
            Stream.concat(
                Arrays.stream(COMMON_CONFIG_OVERRIDES),
                Stream.of(ConfigOverride.config("server.requestLog.appenders[0].logFormat", LOG_FORMAT))
            ));

        // Java HTTP client useful for avoiding Jersey client's auto HTTP header line merging behavior
        private java.net.http.HttpClient javaHttpClient;

        @BeforeAll
        public void setUp() throws Exception {
            testSupport.before();
            javaHttpClient = java.net.http.HttpClient.newHttpClient();
        }

        @AfterAll
        public void tearDown() {
            testSupport.after();
            // TODO: When Java21 is minimum for build, call httpClient.close()
        }

        @Test
        void request_duplicateLine() throws Exception {
            // given
            URI uri = java.net.URI.create(String.format("http://localhost:%d/greet?name=Charley",
                testSupport.getLocalPort()));

            // when
            java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder()
                .uri(uri)
                .header("X-Foo", "a")
                .header("X-Foo", "b")
                .GET()
                .build();
            javaHttpClient.send(req, java.net.http.HttpResponse.BodyHandlers.discarding());
            sendAndAwaitSentinel(testSupport);

            // then
            List<LogLine> lines = readTestLogLines().stream()
                .map(PARSER::parseLog)
                .toList();
            assertThat(lines)
                .singleElement()
                .satisfies(line ->
                    // Expected: joins repeated request-header values with ","
                    assertThat(line.get("xFoo"))
                        .isEqualTo("a,b"));
        }

        @Test
        void request_commaSeparated() throws Exception {
            // given
            String uri = String.format("http://localhost:%d/greet?name=Charley", testSupport.getLocalPort());

            // when
            //    Send a single wire line "X-Foo: a,b" (client concatenation of the value, not two header lines).
            testSupport.getClient()
                .target(uri)
                .request()
                .header("X-Foo", "a,b")
                .get()
                .close();
            sendAndAwaitSentinel(testSupport);

            // then
            List<LogLine> lines = readTestLogLines().stream()
                .map(PARSER::parseLog)
                .toList();
            assertThat(lines)
                .singleElement()
                .satisfies(line ->
                    // Same observable result as the duplicate-lines case - the join is at the servlet-API level.
                    assertThat(line.get("xFoo"))
                        .isEqualTo("a,b"));
        }

        @Test
        void response_setViaMultipleHeaderCalls() throws Exception {
            // given
            String uri = String.format("http://localhost:%d/greet/echo?respHeader=X-Bar:a&respHeader=X-Bar:b",
                testSupport.getLocalPort());

            // when
            //    /echo accepts repeated respHeader query params; each becomes a separate rb.header(...) call.
            //    Two calls with the same header name produces two response-header lines on the wire.
            testSupport.getClient()
                .target(uri)
                .request()
                .get()
                .close();
            sendAndAwaitSentinel(testSupport);

            // then
            List<LogLine> lines = readTestLogLines().stream()
                .map(PARSER::parseLog)
                .toList();
            assertThat(lines)
                .singleElement()
                .satisfies(line ->
                    // Expected: joins repeated response-header values with ",".
                    assertThat(line.get("xBar"))
                        .isEqualTo("a,b"));
        }

        @Test
        void multiValuedResponseHeader_commaSeparatedSingleCall_passesThrough() throws Exception {
            // given
            String uri = String.format("http://localhost:%d/greet/echo?respHeader=X-Bar:a,b",
                testSupport.getLocalPort());

            // when
            //    Symmetric to the request-side comma-separated wire test: server sets a single response header
            //    whose value already contains the comma-joined list. Should pass through unchanged.
            testSupport.getClient()
                .target(uri)
                .request()
                .get()
                .close();
            sendAndAwaitSentinel(testSupport);

            // then
            List<LogLine> lines = readTestLogLines().stream()
                .map(PARSER::parseLog)
                .toList();
            assertThat(lines)
                .singleElement()
                .satisfies(line ->
                    assertThat(line.get("xBar"))
                        .isEqualTo("a,b"));
        }

        @Test
        void caseInsensitive_requestHeader_lookup() throws Exception {
            // given
            String uri = String.format("http://localhost:%d/greet?name=Charley", testSupport.getLocalPort());

            // when
            testSupport.getClient()
                .target(uri)
                .request()
                .header(HttpHeaders.USER_AGENT, "case-test-agent")
                .get()
                .close();
            sendAndAwaitSentinel(testSupport);

            // then
            List<LogLine> lines = readTestLogLines().stream()
                .map(PARSER::parseLog)
                .toList();
            assertThat(lines)
                .singleElement()
                .satisfies(line -> {
                    // Both %i{User-Agent} and %i{user-agent} resolve to the same value; headers case-insensitive
                    assertThat(line.get("userAgentCanonical"))
                        .isEqualTo("case-test-agent");
                    assertThat(line.get("userAgentLower"))
                        .isEqualTo("case-test-agent");
                });
        }

        @Test
        void caseInsensitive_responseHeader_lookup() throws Exception {
            // given
            String uri = String.format("http://localhost:%d/greet?name=Charley", testSupport.getLocalPort());

            // when
            testSupport.getClient()
                .target(uri)
                .request()
                .get()
                .close();
            sendAndAwaitSentinel(testSupport);

            // then
            List<LogLine> lines = readTestLogLines().stream()
                .map(PARSER::parseLog)
                .toList();
            assertThat(lines)
                .singleElement()
                .satisfies(line -> {
                    // Both %responseHeader{Content-Type} and %responseHeader{content-type} resolve to the same value;
                    // headers case-insensitive
                    assertThat(line.get("contentTypeCanonical"))
                        .isEqualTo("text/plain");
                    assertThat(line.get("contentTypeLower"))
                        .isEqualTo("text/plain");
                });
        }
    }

    // TeeFilter (logback-access) is a servlet filter that buffers request and response bodies so
    // %requestContent and %responseContent can render them. Dropwizard does not install it by default -
    // the kitchen sink locks the "not installed → empty bodies" behavior; this class covers the "installed
    // → bodies present" path.
    //
    // Uses a specialized TestApplication subclass so TeeFilter's stream wrapping doesn't affect other tests.
    public static class TeeFilterEnabledTestApplication extends TestApplication {
        @Override
        public void run(Configuration configuration, Environment environment) {
            super.run(configuration, environment);
            environment.servlets()
                .addFilter("teeFilter", new TeeFilter())
                .addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/*");
        }
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class TeeFilterEnabled {
        // Fields:
        //   1. %U               - sentinel carrier
        //   2. %requestContent  - request body (only populated when TeeFilter buffered it)
        //   3. %responseContent - response body (only populated when TeeFilter buffered it)
        private static final String LOG_FORMAT = "%U|%requestContent|%responseContent";

        private static final RequestLogParser PARSER = new RequestLogParser(
              "(?<uri>[^|]*)\\|"
            + "(?<requestContent>[^|]*)\\|"
            + "(?<responseContent>[^|]*)");

        // Use the TeeFilter-enabled app subclass so its stream-wrapping is scoped to this nested class.
        private final DropwizardTestSupport<Configuration> testSupport = createAppTestSupport(
            TeeFilterEnabledTestApplication.class,
            Stream.concat(
                Arrays.stream(COMMON_CONFIG_OVERRIDES),
                Stream.of(ConfigOverride.config("server.requestLog.appenders[0].logFormat", LOG_FORMAT))
            ));

        @BeforeAll
        public void setUp() throws Exception {
            testSupport.before();
        }

        @AfterAll
        public void tearDown() {
            testSupport.after();
        }

        @Test
        void requestAndResponseContent_areCapturedWithTeeFilterInstalled() throws Exception {
            // given
            String uri = String.format("http://localhost:%d/greet/submit", testSupport.getLocalPort());

            // when
            //    POST a small JSON body
            testSupport.getClient()
                .target(uri)
                .request()
                .post(Entity.entity(new PostParameter("hi"), MediaType.APPLICATION_JSON_TYPE))
                .close();
            sendAndAwaitSentinel(testSupport);

            // then
            List<LogLine> lines = readTestLogLines().stream()
                .map(PARSER::parseLog)
                .toList();
            assertThat(lines)
                .singleElement()
                .satisfies(line -> {
                    // Request body captured - JSON serialization of PostParameter("hi")
                    assertThat(line.get("requestContent"))
                        .isEqualTo("{\"param\":\"hi\"}");
                    // Response body captured - from submitSomething's return value
                    assertThat(line.get("responseContent"))
                        .isEqualTo("Munch munch");
                });
        }

        // --- Charset handling ---
        //
        // The discriminator character is é (U+00E9). It encodes differently:
        //   UTF-8:      0xC3 0xA9 (2 bytes)
        //   ISO-8859-1: 0xE9      (1 byte)
        // If JettyAccessEvent resolves the charset correctly, the log line contains "é". If it decodes ISO-8859-1
        // bytes as UTF-8 (or vice versa), the log line contains garbage instead.

        private static final String E_ACUTE_BODY = "hello é";

        // --- Request-side charset resolution ---

        @Test
        void requestContent_isDecodedUsingContentTypeCharset_explicitUtf8() throws Exception {
            // given
            String uri = String.format("http://localhost:%d/greet/echo", testSupport.getLocalPort());
            byte[] bodyBytes = E_ACUTE_BODY.getBytes(StandardCharsets.UTF_8);

            // when
            testSupport.getClient()
                .target(uri)
                .request()
                .post(Entity.entity(bodyBytes, "text/plain;charset=UTF-8"))
                .close();
            sendAndAwaitSentinel(testSupport);

            // then
            List<LogLine> lines = readTestLogLines().stream()
                .map(PARSER::parseLog)
                .toList();
            assertThat(lines)
                .singleElement()
                .satisfies(line ->
                    assertThat(line.get("requestContent"))
                        .isEqualTo(E_ACUTE_BODY));
        }

        @Test
        void requestContent_isDecodedUsingContentTypeCharset_explicitIso88591() throws Exception {
            // given
            String uri = String.format("http://localhost:%d/greet/echo", testSupport.getLocalPort());
            byte[] bodyBytes = E_ACUTE_BODY.getBytes(StandardCharsets.ISO_8859_1);

            // when
            testSupport.getClient()
                .target(uri)
                .request()
                .post(Entity.entity(bodyBytes, "text/plain;charset=ISO-8859-1"))
                .close();
            sendAndAwaitSentinel(testSupport);

            // then
            //    If JettyAccessEvent read the charset from the request Content-Type header, it decodes the single
            //    0xE9 byte as ISO-8859-1's 'é' and the log matches. If it defaulted to UTF-8, 0xE9 alone is invalid
            //    and the decoded string would contain a replacement character instead.
            List<LogLine> lines = readTestLogLines().stream()
                .map(PARSER::parseLog)
                .toList();
            assertThat(lines)
                .singleElement()
                .satisfies(line ->
                    assertThat(line.get("requestContent"))
                        .isEqualTo(E_ACUTE_BODY));
        }

        @Test
        void requestContent_defaultsToUtf8_whenNoContentTypeCharset() throws Exception {
            // given
            //    Assumption: body is UTF-8 encoded so the default matches the encoding.
            //    MediaType.WILDCARD (*/*) causes Jersey to omit charset info; the request Content-Type header on
            //    the wire will be "*/*" with no charset param, so JettyAccessEvent falls through to its UTF-8 default.
            String uri = String.format("http://localhost:%d/greet/echo", testSupport.getLocalPort());
            byte[] bodyBytes = E_ACUTE_BODY.getBytes(StandardCharsets.UTF_8);

            // when
            testSupport.getClient()
                .target(uri)
                .request()
                .post(Entity.entity(bodyBytes, MediaType.WILDCARD))
                .close();
            sendAndAwaitSentinel(testSupport);

            // then
            List<LogLine> lines = readTestLogLines().stream()
                .map(PARSER::parseLog)
                .toList();
            assertThat(lines)
                .singleElement()
                .satisfies(line ->
                    assertThat(line.get("requestContent"))
                        .isEqualTo(E_ACUTE_BODY));
        }

        @Test
        void requestContent_defaultsToUtf8_whenContentTypeHasNoCharsetParam() throws Exception {
            // given
            String uri = String.format("http://localhost:%d/greet/echo", testSupport.getLocalPort());
            byte[] bodyBytes = E_ACUTE_BODY.getBytes(StandardCharsets.UTF_8);

            // when
            testSupport.getClient()
                .target(uri)
                .request()
                .post(Entity.entity(bodyBytes, "text/plain"))
                .close();
            sendAndAwaitSentinel(testSupport);

            // then
            List<LogLine> lines = readTestLogLines().stream()
                .map(PARSER::parseLog)
                .toList();
            assertThat(lines)
                .singleElement()
                .satisfies(line ->
                    assertThat(line.get("requestContent"))
                        .isEqualTo(E_ACUTE_BODY));
        }

        // Note: no request-side test for a malformed Content-Type charset. Jetty's eagerly validates the charset and
        // throws UnsupportedCharsetException / IllegalCharsetNameException, which surfaces as a 500 before the request
        // reaches our resource. On the other hand, response-side malformed-charset is testable; see below.)

        // --- Response-side charset resolution ---

        @Test
        void responseContent_isDecodedUsingContentTypeCharset_explicitUtf8() throws Exception {
            // given
            String uri = String.format(
                "http://localhost:%d/greet/echo?body=%s&contentType=text/plain&responseCharset=UTF-8",
                testSupport.getLocalPort(),
                URLEncoder.encode(E_ACUTE_BODY, StandardCharsets.UTF_8));

            // when
            testSupport.getClient()
                .target(uri)
                .request()
                .get()
                .close();
            sendAndAwaitSentinel(testSupport);

            // then
            List<LogLine> lines = readTestLogLines().stream()
                .map(PARSER::parseLog)
                .toList();
            assertThat(lines)
                .singleElement()
                .satisfies(line ->
                    assertThat(line.get("responseContent"))
                        .isEqualTo(E_ACUTE_BODY));
        }

        @Test
        void responseContent_isDecodedUsingContentTypeCharset_explicitIso88591() throws Exception {
            // given
            String uri = String.format(
                "http://localhost:%d/greet/echo?body=%s&contentType=text/plain&responseCharset=ISO-8859-1",
                testSupport.getLocalPort(),
                URLEncoder.encode(E_ACUTE_BODY, StandardCharsets.UTF_8));

            // when
            testSupport.getClient()
                .target(uri)
                .request()
                .get()
                .close();
            sendAndAwaitSentinel(testSupport);

            // then
            List<LogLine> lines = readTestLogLines().stream()
                .map(PARSER::parseLog)
                .toList();
            assertThat(lines)
                .singleElement()
                .satisfies(line ->
                    assertThat(line.get("responseContent"))
                        .isEqualTo(E_ACUTE_BODY));
        }

        // Note: no test for a response with no Content-Type header at all. JAX-RS always sets one (defaults to
        // application/octet-stream for byte[] entities when @Produces isn't set), so the scenario is unreachable
        // through normal Dropwizard/JAX-RS code paths.

        @Test
        void responseContent_defaultsToUtf8_whenContentTypeHasNoCharsetParam() throws Exception {
            // given
            //    contentType=text/plain but no responseCharset - server sends UTF-8 body bytes with plain
            //    "text/plain" Content-Type (no charset param).
            String uri = String.format("http://localhost:%d/greet/echo?body=%s&contentType=text/plain",
                testSupport.getLocalPort(),
                URLEncoder.encode(E_ACUTE_BODY, StandardCharsets.UTF_8));

            // when
            testSupport.getClient()
                .target(uri)
                .request()
                .get()
                .close();
            sendAndAwaitSentinel(testSupport);

            // then
            List<LogLine> lines = readTestLogLines().stream()
                .map(PARSER::parseLog)
                .toList();
            assertThat(lines)
                .singleElement()
                .satisfies(line ->
                    assertThat(line.get("responseContent"))
                        .isEqualTo(E_ACUTE_BODY));
        }

        @Test
        void responseContent_defaultsToUtf8_whenContentTypeHasMalformedCharset() throws Exception {
            // given
            //    Server sends UTF-8 body bytes but declares a bogus charset. JettyAccessEvent should catch the
            //    exception in resolveResponseCharset and fall back to UTF-8.
            String uri = String.format(
                "http://localhost:%d/greet/echo?body=%s&contentType=text/plain;charset=XYZ-INVALID-CHARSET",
                testSupport.getLocalPort(),
                URLEncoder.encode(E_ACUTE_BODY, StandardCharsets.UTF_8));

            // when
            testSupport.getClient()
                .target(uri)
                .request()
                .get()
                .close();
            sendAndAwaitSentinel(testSupport);

            // then
            List<LogLine> lines = readTestLogLines().stream()
                .map(PARSER::parseLog)
                .toList();
            assertThat(lines)
                .singleElement()
                .satisfies(line ->
                    assertThat(line.get("responseContent"))
                        .isEqualTo(E_ACUTE_BODY));
        }
    }

    // Specialized TestApplication that installs a Jetty SessionHandler so tests exercising %S / getSession
    // work. Dropwizard doesn't attach one by default. Kept in a specialized subclass so the session-tracking
    // side effects (JSESSIONID cookies, potential eager session creation) don't affect other tests.
    public static class SessionEnabledTestApplication extends TestApplication {
        @Override
        public void run(Configuration configuration, Environment environment) {
            environment.servlets().setSessionHandler(new SessionHandler());
            super.run(configuration, environment);
        }
    }

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class SessionEnabledApp {
        // Fields:
        //   1. %U - sentinel carrier
        //   2. %S - session ID
        private static final String LOG_FORMAT = "%U|%S";

        private static final RequestLogParser PARSER = new RequestLogParser(
              "(?<uri>[^|]*)\\|"
            + "(?<sessionID>[^|]*)");

        private final DropwizardTestSupport<Configuration> testSupport = createAppTestSupport(
            SessionEnabledTestApplication.class,
            Stream.concat(
                Arrays.stream(COMMON_CONFIG_OVERRIDES),
                Stream.of(ConfigOverride.config("server.requestLog.appenders[0].logFormat", LOG_FORMAT))
            ));

        @BeforeAll
        public void setUp() throws Exception {
            testSupport.before();
        }

        @AfterAll
        public void tearDown() {
            testSupport.after();
        }

        @Test
        void sessionID_isPopulatedWhenRequestCreatesSession() throws Exception {
            // given
            String uri = String.format("http://localhost:%d/greet/session", testSupport.getLocalPort());

            // when
            //    hit /greet/session which calls req.getSession(true)
            testSupport.getClient()
                .target(uri)
                .request()
                .get()
                .close();
            sendAndAwaitSentinel(testSupport);

            // then
            //    %S should be a non-empty, non-"-" session id
            List<LogLine> lines = readTestLogLines().stream().map(PARSER::parseLog).toList();
            assertThat(lines)
                .singleElement()
                .satisfies(line ->
                    assertThat(line.get("sessionID"))
                        .isNotEqualTo("-")
                        .isNotBlank());
        }
    }

    // Exercises IAccessEvent.getRequest() / getResponse() end-to-end. This helps us "indirectly" verify that
    // Logback Access's AccessEventDiscriminator can theoretically work properly with our logging implementation.
    //
    // Testing directly is difficult since Dropwizard doesn't have configs to currently spin up an app with that
    // configuration. At the same time, we'd like to verify that our IAccessEvent behaves properly as implemented.
    //
    // We attach a synchronous appender directly to the LogbackAccessRequestLog so we can capture the IAccessEvent on
    // the request thread (before Jetty could recycle the request), then invoke the getters synchronously and assert.
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class ServletRequestResponseAccessors {
        private final DropwizardTestSupport<Configuration> testSupport = createAppTestSupport(
            SessionEnabledTestApplication.class,
            Stream.of(COMMON_CONFIG_OVERRIDES));

        private volatile Consumer<? super IAccessEvent> assertions;
        private volatile boolean assertionsRan;
        private volatile Throwable capturedFailure;

        @BeforeAll
        public void setUp() throws Exception {
            testSupport.before();

            // Attach a capturing appender directly to the LogbackAccessRequestLog. Because we don't wrap in
            // AsyncAppender, doAppend runs synchronously on Jetty's request thread - event.getRequest() /
            // event.getResponse() are safe to call inside doAppend.
            RequestLog requestLog = testSupport.getEnvironment().getApplicationContext().getServer().getRequestLog();
            if (!(requestLog instanceof LogbackAccessRequestLog accessLog)) {
                throw new IllegalStateException("Expected LogbackAccessRequestLog, was: " + requestLog.getClass());
            }
            AppenderBase<IAccessEvent> capturing = new AppenderBase<>() {
                @Override
                protected void append(IAccessEvent eventObject) {
                    try {
                        assertions.accept(eventObject);
                    } catch (Throwable t) {
                        capturedFailure = t;
                    }
                }
            };
            capturing.setName("capturingAppender");
            capturing.setContext(new ContextBase());
            capturing.start();
            accessLog.addAppender(capturing);
        }

        @AfterAll
        public void tearDown() {
            testSupport.after();
        }

        @BeforeEach
        public void resetState() {
            assertions = event -> {
                Assertions.fail("Forgot to initialize assertions for this test case");
            };
            assertionsRan = false;
            capturedFailure = null;
        }

        @AfterEach
        public void checkAssertionsRanAndBackgroundFailure() {
            final Throwable localCapturedFailure = capturedFailure;
            if (localCapturedFailure != null) {
                throw new AssertionError(localCapturedFailure);
            }

            assertThat(assertionsRan)
                .isTrue();
        }

        @Test
        void getRequest_returnsServletRequest_andReflectsSession() {
            assertions = event -> {
                if (event.getRequestURI().startsWith("/sentinel")) {
                    return;
                }

                // then
                //    getRequest() returns a real servlet request
                HttpServletRequest servletRequest = event.getRequest();
                assertThat(servletRequest)
                    .isNotNull();
                //    ... and its session is populated (because LogbackAccessRequestLogAwareHandler pinned the
                //    session-aware ServletContextRequest as the logged request).
                HttpSession session = servletRequest.getSession(false);
                assertThat(session)
                    .isNotNull();
                assertThat(session.getId())
                    .isNotBlank();

                assertionsRan = true;
            };

            // given
            String uri = String.format("http://localhost:%d/greet/session", testSupport.getLocalPort());

            // when
            //    hit an endpoint that creates a session on the request
            testSupport.getClient()
                .target(uri)
                .request()
                .get()
                .close();
            sendAndAwaitSentinel(testSupport);
        }

        @Test
        void getResponse_returnsServletResponse_andReflectsStatus() {
            assertions = event -> {
                if (event.getRequestURI().startsWith("/sentinel")) {
                    return;
                }

                // then
                HttpServletResponse servletResponse = event.getResponse();
                assertThat(servletResponse)
                    .isNotNull();
                assertThat(servletResponse.getStatus())
                    .isEqualTo(418);

                assertionsRan = true;
            };

            // given
            String uri = String.format("http://localhost:%d/greet/echo?status=418&body=teapot",
                testSupport.getLocalPort());

            // when
            testSupport.getClient()
                .target(uri)
                .request()
                .get()
                .close();
            sendAndAwaitSentinel(testSupport);
        }
    }
}
