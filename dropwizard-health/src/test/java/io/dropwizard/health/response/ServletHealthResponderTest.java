package io.dropwizard.health.response;

import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpTester;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlet.ServletTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.StringJoiner;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServletHealthResponderTest {
    private static final String NO_STORE = "no-store";
    private static final String HEALTH_CHECK_URI = "/health-check";
    private static final HealthResponse SUCCESS = new HealthResponse(true, "healthy", MediaType.TEXT_PLAIN);
    private static final HealthResponse FAIL = new HealthResponse(false, "unhealthy", MediaType.TEXT_PLAIN);

    private final HttpTester.Request request = new HttpTester.Request();

    private ServletTester servletTester;

    @Mock
    private HealthResponseProvider healthResponseProvider;

    @BeforeEach
    public void setUp() throws Exception {
        servletTester = new ServletTester();

        request.setHeader(HttpHeader.HOST.asString(), "localhost");
        request.setURI(HEALTH_CHECK_URI);
        request.setMethod(HttpMethod.GET.asString());
    }

    @AfterEach
    void tearDown() throws Exception {
        servletTester.stop();
    }

    @Test
    void shouldReturnHealthyWithNoParametersAndCacheControlDisabled() throws Exception {
        // given
        final ServletHealthResponder servletHealthResponder = new ServletHealthResponder(healthResponseProvider, false,
            "no-store");

        // when
        when(healthResponseProvider.minimalHealthResponse(isNull())).thenReturn(SUCCESS);
        servletTester.addServlet(new ServletHolder(servletHealthResponder), HEALTH_CHECK_URI);
        servletTester.start();
        final HttpTester.Response response = executeRequest(request);

        // then
        assertThat(response.getStatus()).isEqualTo(Response.SC_OK);
        assertThat(response.get(HttpHeader.CACHE_CONTROL)).isNull();
    }

    @Test
    void shouldReturnHealthyWithNoParametersProvided() throws Exception {
        // given
        final ServletHealthResponder servletHealthResponder = new ServletHealthResponder(healthResponseProvider, true,
            "no-store");

        // when
        when(healthResponseProvider.minimalHealthResponse(isNull())).thenReturn(SUCCESS);
        servletTester.addServlet(new ServletHolder(servletHealthResponder), HEALTH_CHECK_URI);
        servletTester.start();
        final HttpTester.Response response = executeRequest(request);

        // then
        assertThat(response.getStatus()).isEqualTo(Response.SC_OK);
        assertThat(response.get(HttpHeader.CACHE_CONTROL))
            .isNotNull()
            .isEqualTo(NO_STORE);
    }

    @Test
    void shouldReturnHealthyWithAliveTypeProvided() throws Exception {
        // given
        final ServletHealthResponder servletHealthResponder = new ServletHealthResponder(healthResponseProvider, true,
            "no-store");

        // when
        when(healthResponseProvider.minimalHealthResponse(eq("alive"))).thenReturn(SUCCESS);
        servletTester.addServlet(new ServletHolder(servletHealthResponder), HEALTH_CHECK_URI);
        servletTester.start();

        request.setURI(String.format("%s?%s=%s", HEALTH_CHECK_URI, ServletHealthResponder.CHECK_TYPE_QUERY_PARAM, "alive"));
        final HttpTester.Response response = executeRequest(request);

        // then
        assertThat(response.getStatus()).isEqualTo(Response.SC_OK);
        assertThat(response.get(HttpHeader.CACHE_CONTROL))
            .isNotNull()
            .isEqualTo(NO_STORE);
    }

    @Test
    void shouldReturnUnhealthyWithNoParametersProvided() throws Exception {
        // given
        final ServletHealthResponder servletHealthResponder = new ServletHealthResponder(healthResponseProvider, true,
            "no-store");

        // when
        when(healthResponseProvider.minimalHealthResponse(isNull())).thenReturn(FAIL);
        servletTester.addServlet(new ServletHolder(servletHealthResponder), HEALTH_CHECK_URI);
        servletTester.start();
        final HttpTester.Response response = executeRequest(request);

        // then
        assertThat(response.getStatus()).isEqualTo(Response.SC_SERVICE_UNAVAILABLE);
        assertThat(response.get(HttpHeader.CACHE_CONTROL))
            .isNotNull()
            .isEqualTo(NO_STORE);
    }

    @Test
    void shouldReturnAllHealthCheckResultsWhenAllParameterIsProvided() throws Exception {
        // given
        final ServletHealthResponder servletHealthResponder = new ServletHealthResponder(healthResponseProvider, true,
            "no-store");

        // when
        when(healthResponseProvider.fullHealthResponse(eq("alive"))).thenReturn(SUCCESS);
        servletTester.addServlet(new ServletHolder(servletHealthResponder), HEALTH_CHECK_URI);
        servletTester.start();
        final String uri = String.format("%s?%s=%s&name=all", HEALTH_CHECK_URI,
            ServletHealthResponder.CHECK_TYPE_QUERY_PARAM, "alive");
        request.setURI(uri);
        final HttpTester.Response response = executeRequest(request);

        // then
        assertThat(response.getStatus()).isEqualTo(Response.SC_OK);
        assertThat(response.getContent()).isEqualTo(SUCCESS.getMessage().orElseThrow(IllegalStateException::new));
        assertThat(response.get(HttpHeader.CONTENT_TYPE)).startsWith(SUCCESS.getContentType());
        assertThat(response.get(HttpHeader.CACHE_CONTROL))
            .isNotNull()
            .isEqualTo(NO_STORE);
    }

    @Test
    void shouldReturnSpecificHealthCheckResultsWhenNameParametersAreProvided() throws Exception {
        // given
        final ServletHealthResponder servletHealthResponder = new ServletHealthResponder(healthResponseProvider, true,
            "no-store");
        final List<String> names = Arrays.stream(new String[]{"foo", "bar", "baz"}).collect(Collectors.toList());

        // when
        when(healthResponseProvider.partialHealthResponse(eq("alive"), eq(names))).thenReturn(SUCCESS);
        servletTester.addServlet(new ServletHolder(servletHealthResponder), HEALTH_CHECK_URI);
        servletTester.start();

        final StringJoiner joiner = new StringJoiner("&");
        names.forEach(name -> joiner.add("name=" + name));
        final String uri = String.format("%s?%s=%s&%s", HEALTH_CHECK_URI, ServletHealthResponder.CHECK_TYPE_QUERY_PARAM,
            "alive", joiner);
        request.setURI(uri);
        final HttpTester.Response response = executeRequest(request);

        // then
        assertThat(response.getStatus()).isEqualTo(Response.SC_OK);
        assertThat(response.getContent()).isEqualTo(SUCCESS.getMessage().orElseThrow(IllegalStateException::new));
        assertThat(response.get(HttpHeader.CONTENT_TYPE)).startsWith(SUCCESS.getContentType());
        assertThat(response.get(HttpHeader.CACHE_CONTROL))
            .isNotNull()
            .isEqualTo(NO_STORE);
    }

    @Test
    void shouldReturnSingleHealthCheckResultWhenOneNameParameterProvided() throws Exception {
        // given
        final ServletHealthResponder servletHealthResponder = new ServletHealthResponder(healthResponseProvider, true,
            "no-store");
        final String name = "foo";

        // when
        when(healthResponseProvider.partialHealthResponse(eq("alive"),
            eq(Collections.singletonList(name)))).thenReturn(SUCCESS);
        servletTester.addServlet(new ServletHolder(servletHealthResponder), HEALTH_CHECK_URI);
        servletTester.start();

        final String uri = String.format("%s?%s=%s&name=%s", HEALTH_CHECK_URI,
            ServletHealthResponder.CHECK_TYPE_QUERY_PARAM,
            "alive", name);
        request.setURI(uri);
        final HttpTester.Response response = executeRequest(request);

        // then
        assertThat(response.getStatus()).isEqualTo(Response.SC_OK);
        assertThat(response.getContent()).isEqualTo(SUCCESS.getMessage().orElseThrow(IllegalStateException::new));
        assertThat(response.get(HttpHeader.CONTENT_TYPE)).startsWith(SUCCESS.getContentType());
        assertThat(response.get(HttpHeader.CACHE_CONTROL))
            .isNotNull()
            .isEqualTo(NO_STORE);
    }

    @Test
    void shouldReturnAllHealthCheckResultsWhenBothSpecificNamesAndAllParametersProvided() throws Exception {
        // given
        final ServletHealthResponder servletHealthResponder = new ServletHealthResponder(healthResponseProvider, true,
            "no-store");
        final List<String> names = Arrays.stream(new String[]{"foo", "bar", "baz"}).collect(Collectors.toList());

        // when
        when(healthResponseProvider.fullHealthResponse(eq("alive"))).thenReturn(SUCCESS);
        servletTester.addServlet(new ServletHolder(servletHealthResponder), HEALTH_CHECK_URI);
        servletTester.start();

        final StringJoiner joiner = new StringJoiner("&");
        names.forEach(name -> joiner.add("name=" + name));
        final String uri = String.format("%s?%s=%s&%s&name=all", HEALTH_CHECK_URI,
            ServletHealthResponder.CHECK_TYPE_QUERY_PARAM, "alive", joiner);
        request.setURI(uri);
        final HttpTester.Response response = executeRequest(request);

        // then
        assertThat(response.getStatus()).isEqualTo(Response.SC_OK);
        assertThat(response.getContent()).isEqualTo(SUCCESS.getMessage().orElseThrow(IllegalStateException::new));
        assertThat(response.get(HttpHeader.CONTENT_TYPE)).startsWith(SUCCESS.getContentType());
        assertThat(response.get(HttpHeader.CACHE_CONTROL))
            .isNotNull()
            .isEqualTo(NO_STORE);
    }

    private HttpTester.Response executeRequest(HttpTester.Request request) throws Exception {
        return HttpTester.parseResponse(servletTester.getResponses(request.generate()));
    }
}
