package com.example.request_log;

import com.example.request_log.helper.RequestLogParser;
import io.dropwizard.core.Configuration;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.stream.Stream;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ClassicRequestLogIntegrationTest extends AbstractRequestLogIntegrationTest {
    private static final String USER_AGENT = "TestApplication (test-request-logs)";

    private static final ConfigOverride[] COMMON_CONFIG_OVERRIDES = new ConfigOverride[]{
        ConfigOverride.config("server.requestLog.type", "classic")};

    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    @Nested
    class DefaultAppConfig {
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
            List<RequestLogParser.LogLine> parsedLogs = readTestLogLines().stream()
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
                        // %U in Jetty CustomRequestLog is path only; query string would need %q
                        .isEqualTo("/greet");
                    assertThat(line.getProtocol())
                        .isEqualTo("HTTP/1.1");
                    assertThat(line.getStatus())
                        .isEqualTo("200");
                    // bytes field sufficiently asserted during parsing
                    assertThat(line.getReferer())
                        // possibly less than ideal behavior by Jetty's logging impl
                        .isEqualTo("");
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
            String basicAuthHeader1 = "Basic " + Base64.getEncoder().encodeToString(basicAuth1.getBytes(UTF_8));
            String username2 = "user2";
            String password2 = "";
            String basicAuth2 = String.format("%s:%s", username2, password2);
            String basicAuthHeader2 = "Basic " + Base64.getEncoder().encodeToString(basicAuth2.getBytes(UTF_8));
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
            List<RequestLogParser.LogLine> parsedLogs = readTestLogLines().stream()
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
                        // possibly less than ideal behavior by Jetty's logging impl
                        .isEqualTo("");
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
            List<RequestLogParser.LogLine> parsedLogs = readTestLogLines().stream()
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
            List<RequestLogParser.LogLine> parsedLogs = readTestLogLines().stream()
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
            List<RequestLogParser.LogLine> parsedLogs = readTestLogLines().stream()
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
            List<RequestLogParser.LogLine> parsedLogs = readTestLogLines().stream()
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
                        // %U in Jetty CustomRequestLog is path only; query string would need %q
                        .isEqualTo("/greet");
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
}
