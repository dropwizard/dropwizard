package io.dropwizard.health.response;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.core.MediaType;
import org.eclipse.jetty.ee10.servlet.ServletHolder;
import org.eclipse.jetty.ee10.servlet.ServletTester;
import org.eclipse.jetty.http.HttpHeader;
import org.eclipse.jetty.http.HttpMethod;
import org.eclipse.jetty.http.HttpTester;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.util.Collections.unmodifiableList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServletHealthResponderTest {
    private static final String NO_STORE = "no-store";
    private static final String HEALTH_CHECK_URI = "/health-check";
    private static final HealthResponse SUCCESS = new HealthResponse(true, "healthy", MediaType.TEXT_PLAIN,
        HttpServletResponse.SC_OK);
    private static final HealthResponse FAIL = new HealthResponse(false, "unhealthy", MediaType.TEXT_PLAIN,
        HttpServletResponse.SC_SERVICE_UNAVAILABLE);

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
    void shouldReturnHealthyWithCacheControlDisabled() throws Exception {
        // given
        final ServletHealthResponder servletHealthResponder = new ServletHealthResponder(healthResponseProvider, false,
            "no-store");

        // when
        when(healthResponseProvider.healthResponse(Collections.emptyMap())).thenReturn(SUCCESS);
        servletTester.addServlet(new ServletHolder(servletHealthResponder), HEALTH_CHECK_URI);
        servletTester.start();
        final HttpTester.Response response = executeRequest(request);

        // then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        assertThat(response.get(HttpHeader.CACHE_CONTROL)).isNull();
    }

    @Test
    void shouldReturnHealthyWithNoParametersProvided() throws Exception {
        // given
        final ServletHealthResponder servletHealthResponder = new ServletHealthResponder(healthResponseProvider, true,
            "no-store");

        // when
        when(healthResponseProvider.healthResponse(Collections.emptyMap())).thenReturn(SUCCESS);
        servletTester.addServlet(new ServletHolder(servletHealthResponder), HEALTH_CHECK_URI);
        servletTester.start();
        final HttpTester.Response response = executeRequest(request);

        // then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
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
        when(healthResponseProvider.healthResponse(Collections.emptyMap())).thenReturn(FAIL);
        servletTester.addServlet(new ServletHolder(servletHealthResponder), HEALTH_CHECK_URI);
        servletTester.start();
        final HttpTester.Response response = executeRequest(request);

        // then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        assertThat(response.get(HttpHeader.CACHE_CONTROL))
            .isNotNull()
            .isEqualTo(NO_STORE);
    }

    @Test
    void shouldReturnHealthResultsWhenMultipleParametersProvided() throws Exception {
        // given
        final ServletHealthResponder servletHealthResponder = new ServletHealthResponder(healthResponseProvider, true,
            "no-store");
        final String typeQueryParam = "type";
        final String type = "alive";
        final String nameQueryParam = "name";
        final String name = "all";
        final String anotherName = "foo";
        final Map<String, Collection<String>> queryParams = new HashMap<>();
        final List<String> nameQueryParams = new ArrayList<>();
        nameQueryParams.add(name);
        nameQueryParams.add(anotherName);
        queryParams.put(typeQueryParam, Collections.singletonList(type));
        queryParams.put(nameQueryParam, unmodifiableList(nameQueryParams));

        // when
        when(healthResponseProvider.healthResponse(queryParams)).thenReturn(SUCCESS);
        servletTester.addServlet(new ServletHolder(servletHealthResponder), HEALTH_CHECK_URI);
        servletTester.start();
        final String uri = String.format("%s?%s=%s&%s=%s&%s=%s", HEALTH_CHECK_URI, typeQueryParam, type, nameQueryParam,
            name, nameQueryParam, anotherName);
        request.setURI(uri);
        final HttpTester.Response response = executeRequest(request);

        // then
        assertThat(response.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        assertThat(response.getContent()).isEqualTo(SUCCESS.getMessage());
        assertThat(response.get(HttpHeader.CONTENT_TYPE)).startsWith(SUCCESS.getContentType());
        assertThat(response.get(HttpHeader.CACHE_CONTROL))
            .isNotNull()
            .isEqualTo(NO_STORE);
    }

    private HttpTester.Response executeRequest(HttpTester.Request request) throws Exception {
        return HttpTester.parseResponse(servletTester.getResponses(request.generate()));
    }
}
