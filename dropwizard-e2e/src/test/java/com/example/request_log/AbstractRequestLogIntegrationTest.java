package com.example.request_log;

import com.codahale.metrics.health.HealthCheck;
import com.example.request_log.helper.RequestLogParser;
import com.example.request_log.helper.RequestLogParser.ExtractionPattern;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.dropwizard.auth.Auth;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.PrincipalImpl;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.DropwizardTestSupport;
import jakarta.annotation.security.PermitAll;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.Provider;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;
import java.util.stream.Stream;

// Conventions for derived classes:
//
// 1. The abstract base is @TestInstance(PER_CLASS) and @Execution(SAME_THREAD). Concrete subclasses inherit both.
//
// 2. In each nested class that owns an app:
//      @TestInstance(TestInstance.Lifecycle.PER_CLASS)
//      @Nested
//      class MyNestedApp {
//          private final DropwizardTestSupport<Configuration> testSupport = createAppTestSupport(...);
//          @BeforeAll public void setUp() throws Exception { testSupport.before(); }
//          @AfterAll  public void tearDown() { testSupport.after(); }
//          ...
//      }
//    The abstract base's @BeforeEach snapshots the log-file line count so readTestLogLines() can return only this
//    test's window.
//
// 3. Ensure your test Application config has:
//      discardingThreshold: 0     # never discard events when the queue is nearly full
//      neverBlock: false          # request threads block when the queue is full rather than dropping events
//    Without this, high-load build systems could lose lines.
//
// 4. Each @Test:
//      a. Make the request(s) whose logs you want to assert on
//      b. sendAndAwaitSentinel(testSupport);          // fires a marker request and blocks until its log line
//                                                     // reaches disk
//      c. List<String> lines = readTestLogLines();    // just this test's lines, sentinel stripped
//      d. Parse and assert
//
// 5. Optionally use RequestLogParser to help with field extraction in preparation for assertions.
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Execution(ExecutionMode.SAME_THREAD)
public abstract class AbstractRequestLogIntegrationTest {
    private static final int SENTINEL_WAIT_SEC = 30;
    private static final String SENTINEL_PATH = "/sentinel";

    // The request log's file appender writes with logback's default charset, which is the JVM's default. Read back
    // with the same charset so a Windows-1252 default (Windows) or UTF-8 default (macOS/Linux) both round-trip cleanly
    private static final Charset LOG_FILE_CHARSET = Charset.defaultCharset();

    protected static final Pattern TIMESTAMP_DATA_PATTERN_STOCK_FORMAT
        = Pattern.compile("\\d{1,2}/\\p{L}{3,9}/\\d{4}:\\d{2}:\\d{2}:\\d{2} [+-]\\d{4}");
    protected static final RequestLogParser REQUEST_LOG_PARSER_STOCK_FORMAT = new RequestLogParser(
        ExtractionPattern.REMOTE_HOST + " "
            + ExtractionPattern.FORWARDING_HOST + " "
            + ExtractionPattern.REMOTE_USER + " "
            + ExtractionPattern.TIMESTAMP + " "
            + "\"" + ExtractionPattern.METHOD + " " + ExtractionPattern.URI + " " + ExtractionPattern.PROTOCOL + "\" "
            + ExtractionPattern.STATUS + " "
            + ExtractionPattern.BYTES + " "
            + "\"" + ExtractionPattern.REFERER + "\" "
            + "\"" + ExtractionPattern.USER_AGENT + "\" "
            + ExtractionPattern.DURATION_MS);
    protected static final String CLIENT_REMOTE_HOST = "127.0.0.1";

    public static class TestApplication extends Application<Configuration> {
        @Override
        public void run(Configuration configuration, Environment environment) {
            environment.jersey().register(TestResource.class);
            environment.jersey().register(SentinelResource.class);
            environment.jersey().register(HeaderToAttributeFilter.class);
            AuthFilter<?, ?> basicAuthFilter = new BasicCredentialAuthFilter.Builder<PrincipalImpl>()
                .setAuthenticator(credentials -> Optional.of(new PrincipalImpl(credentials.getUsername())))
                .setAuthorizer((principal, role, requestContext) -> true)
                .buildAuthFilter();
            environment.jersey().register(new AuthDynamicFeature(basicAuthFilter));
            environment.jersey().register(new AuthValueFactoryProvider.Binder<>(PrincipalImpl.class));
            environment.healthChecks().register("dummy", new HealthCheck() {
                @Override
                protected Result check() {
                    return Result.healthy();
                }
            });
        }
    }

    public static class PostParameter {
        @JsonProperty
        @NotEmpty
        private final String param;

        @JsonCreator
        public PostParameter(@JsonProperty("param") String param) {
            this.param = param;
        }

        public String getParam() {
            return param;
        }
    }

    @Path("/greet")
    public static class TestResource {
        @GET
        public String get(@QueryParam("name") String name) {
            return String.format("Hello, %s!", name);
        }

        @GET
        @PermitAll  // require authn, don't require authz
        @Path("/authenticated")
        public String getAuthenticatedUser(@Auth PrincipalImpl principal) {
            return String.format("Hello, %s!", principal.getName());
        }

        @POST
        @Path("/submit")
        @Produces(MediaType.APPLICATION_JSON)
        @Consumes(MediaType.APPLICATION_JSON)
        public String submitSomething(@NotNull @Valid PostParameter parameter) {
            return "Munch munch";
        }

        // Configurable endpoint for tests that need to drive %s (status), %b (bytes sent), %responseHeader{...},
        // and %responseContent converters. Query params:
        //   status          - response status (default 200)
        //   body            - response body text (default "hello"); its byte length is what %b captures
        //   respHeader      - optional "Name:Value" pair(s) added as response header(s). Repeat the param to add
        //                     multiple response headers (e.g. ?respHeader=X:a&respHeader=X:b sets X twice).
        //   contentType     - optional Content-Type override (e.g. "application/json"). Charset can be included as
        //                     "application/json;charset=ISO-8859-1", or specified separately via responseCharset.
        //   responseCharset - optional charset name to encode the response body with. If set, the body's bytes on
        //                     the wire are body.getBytes(<charset>). Also included in the response's Content-Type
        //                     header if contentType is set (as "<contentType>;charset=<responseCharset>").
        //
        // The endpoint accepts both GET and POST so tests can drive %requestContent by POSTing arbitrary bytes.
        @GET
        @Path("/echo")
        public Response echoGet(
            @QueryParam("status") @DefaultValue("200") int status,
            @QueryParam("body") @DefaultValue("hello") String body,
            @QueryParam("respHeader") List<String> respHeaders,
            @QueryParam("contentType") String contentType,
            @QueryParam("responseCharset") String responseCharsetName) {
            return buildEchoResponse(status, body, respHeaders, contentType, responseCharsetName);
        }

        @POST
        @Path("/echo")
        public Response echoPost(
            @QueryParam("status") @DefaultValue("200") int status,
            @QueryParam("body") @DefaultValue("hello") String body,
            @QueryParam("respHeader") List<String> respHeaders,
            @QueryParam("contentType") String contentType,
            @QueryParam("responseCharset") String responseCharsetName) {
            return buildEchoResponse(status, body, respHeaders, contentType, responseCharsetName);
        }

        private Response buildEchoResponse(int status, String body, List<String> respHeaders,
                                           String contentType, String responseCharsetName) {
            // Encode body in the requested charset if specified; otherwise default UTF-8.
            Charset charset = responseCharsetName != null
                ? Charset.forName(responseCharsetName)
                : StandardCharsets.UTF_8;
            byte[] bodyBytes = body.getBytes(charset);

            Response.ResponseBuilder rb = Response.status(status).entity(bodyBytes);

            if (contentType != null) {
                final String fullContentType = responseCharsetName != null
                    ? contentType + ";charset=" + responseCharsetName
                    : contentType;
                rb.header(HttpHeaders.CONTENT_TYPE, fullContentType);
            }

            for (String respHeader : respHeaders) {
                if (respHeader != null && !respHeader.isBlank()) {
                    final int colon = respHeader.indexOf(':');
                    if (colon > 0) {
                        rb.header(respHeader.substring(0, colon), respHeader.substring(colon + 1));
                    }
                }
            }
            return rb.build();
        }

        // Creates a session on the underlying request so tests can exercise %S (session ID).
        // Returns the created session's ID in the response body for observability, but the tests will usually assert
        // on %S in the log line.
        @GET
        @Path("/session")
        public String createSession(@Context HttpServletRequest req) {
            return req.getSession(true).getId();
        }
    }

    // Sentinel endpoint used by sendAndAwaitSentinel to bound a test's log window
    @Path(SENTINEL_PATH)
    public static class SentinelResource {
        @GET
        @Path("/{marker}")
        public String sentinel(@PathParam("marker") String marker) {
            return marker == null ? "" : marker;
        }
    }

    // Maps well-known request headers to request attributes so log formats can capture them via %reqAttribute{...}.
    // This mimics a load-balancer/proxy fanout you'd see in production:
    //   X-Forwarded-User -> forwardedClient request attribute (authenticated user propagated from proxy)
    //   X-Trace-Id       -> traceId request attribute (distributed-tracing correlation)
    @Provider
    public static class HeaderToAttributeFilter implements ContainerRequestFilter {
        private static final String FORWARDED_CLIENT_ATTRIBUTE = "forwardedClient";
        private static final String TRACE_ID_ATTRIBUTE = "traceId";

        @Context
        private HttpServletRequest servletRequest;

        @Override
        public void filter(ContainerRequestContext ctx) {
            copyHeaderToAttribute(ctx, "X-Forwarded-User", FORWARDED_CLIENT_ATTRIBUTE);
            copyHeaderToAttribute(ctx, "X-Trace-Id", TRACE_ID_ATTRIBUTE);
        }

        private void copyHeaderToAttribute(ContainerRequestContext ctx, String headerName, String attributeName) {
            final String value = ctx.getHeaderString(headerName);
            if (value != null && !value.isBlank()) {
                servletRequest.setAttribute(attributeName, value);
            }
        }
    }

    private java.nio.file.Path requestLogFile;
    private final List<DropwizardTestSupport<Configuration>> createdApps = new ArrayList<>();

    private boolean awaited;
    private int lineCountTestStart;
    private String sentinelMarker;

    @BeforeAll
    public void tempDirSetUp(@TempDir java.nio.file.Path tempDir) {
        requestLogFile = tempDir.resolve("request-logs");
    }

    @AfterAll
    public void tearDownBaseClassChecks() {
        List<DropwizardTestSupport<Configuration>> leakedApps = createdApps.stream()
            .filter(app -> !app.getEnvironment().getApplicationContext().getServer().isStopped())
            .toList();
        for (var app : leakedApps) {
            try {
                app.getEnvironment().getApplicationContext().getServer().stop();
            } catch (Exception e) {
                // best-effort; we're going to throw anyway
            }
        }
        createdApps.clear();

        if (!leakedApps.isEmpty()) {
            throw new IllegalStateException("Derived test forgot to stop " + leakedApps.size()
                + " DropwizardTestSupport instance(s)");
        }
    }

    @BeforeEach
    public void perTestStateSetUp() throws IOException {
        awaited = false;
        lineCountTestStart = countLogLines();
    }

    @AfterEach
    public void perTestStateTearDown() {
        if (!awaited) {
            throw new IllegalStateException("This test did not sendAndAwaitSentinel()");
        }
        sentinelMarker = null;
    }

    public java.nio.file.Path getRequestLogFile() {
        if (requestLogFile == null) {
            throw new IllegalStateException("requestLogFile not yet initialized; @BeforeAll must run first");
        }
        return requestLogFile;
    }

    // Snapshot the current line count for use as a starting bound in readTestLogLines.
    // Safe to call before the log file exists (returns 0).
    private int countLogLines() throws IOException {
        if (!Files.exists(requestLogFile)) {
            return 0;
        }
        return Files.readAllLines(requestLogFile, LOG_FILE_CHARSET).size();
    }

    private List<ConfigOverride> configOverrides() {
        return Collections.singletonList(
            ConfigOverride.config("server.requestLog.appenders[0].currentLogFilename",
                getRequestLogFile().toString()));
    }

    protected DropwizardTestSupport<Configuration> createAppTestSupport(ConfigOverride... extraOverrides) {
        return createAppTestSupport(Stream.of(extraOverrides));
    }

    protected DropwizardTestSupport<Configuration> createAppTestSupport(
        List<? extends ConfigOverride> extraOverrides) {
        return createAppTestSupport(extraOverrides.stream());
    }

    protected DropwizardTestSupport<Configuration> createAppTestSupport(
        Stream<? extends ConfigOverride> extraOverrides) {
        return createAppTestSupport(TestApplication.class, extraOverrides);
    }

    // Overload taking an explicit app class. Use this when a test needs a specialized subclass of
    // TestApplication (e.g. one that registers a servlet filter).
    protected DropwizardTestSupport<Configuration> createAppTestSupport(
        Class<? extends Application<Configuration>> appClass,
        Stream<? extends ConfigOverride> extraOverrides) {
        final List<ConfigOverride> combinedOverrides = Stream.concat(
            configOverrides().stream(),
            extraOverrides
        ).toList();

        DropwizardTestSupport<Configuration> createdApp = new DropwizardTestSupport<>(
            appClass,
            "request_log/config.yml",
            new ResourceConfigurationSourceProvider(),
            combinedOverrides.toArray(new ConfigOverride[0]));
        createdApps.add(createdApp);
        return createdApp;
    }

    // Dispatch a marker request to /sentinel and block until its log line reaches the file. Because the async
    // appender's worker processes events FIFO, seeing the sentinel line implies every request logged prior to the
    // sentinel request is also present on disk. Follow with readTestLogLines() to consume this test's window.
    protected void sendAndAwaitSentinel(DropwizardTestSupport<Configuration> testSupport) {
        if (awaited) {
            throw new IllegalStateException("This test already awaited");
        }
        sendAndAwaitSentinel(testSupport.client(), testSupport.getLocalPort());
    }

    private void sendAndAwaitSentinel(Client client, int localPort) {
        final String marker = "sentinel-" + UUID.randomUUID();
        final String url = String.format("http://localhost:%d%s/%s", localPort, SENTINEL_PATH, marker);
        client.target(url)
            .request()
            .get()
            .close();
        awaitSentinel(marker);
        sentinelMarker = marker;
        awaited = true;
    }

    private void awaitSentinel(String marker) {
        Awaitility.await("sentinel marker '" + marker + "' in " + requestLogFile)
            .atMost(Duration.ofSeconds(SENTINEL_WAIT_SEC))
            .pollInterval(Duration.ofMillis(20))
            .until(() -> Files.exists(requestLogFile)
                && Files.readAllLines(requestLogFile, LOG_FILE_CHARSET).stream()
                    .anyMatch(line -> line.contains(marker)));
    }

    // Read this test's log window - lines produced between @BeforeEach (which snapshots the start
    // position) and the sentinel line (added by sendAndAwaitSentinel). The sentinel line is filtered out.
    protected List<String> readTestLogLines() throws IOException {
        if (!awaited) {
            throw new IllegalStateException("This test has not yet awaited");
        }
        final List<String> all = Files.readAllLines(requestLogFile, LOG_FILE_CHARSET);
        return all.subList(lineCountTestStart, all.size()).stream()
            .filter(l -> !l.contains(sentinelMarker))
            .toList();
    }
}
