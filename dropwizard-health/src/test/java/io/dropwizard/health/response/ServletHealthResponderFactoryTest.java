package io.dropwizard.health.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.health.HealthEnvironment;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.jersey.validation.Validators;
import io.dropwizard.jetty.setup.ServletEnvironment;
import jakarta.servlet.ServletRegistration;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Validator;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static io.dropwizard.health.response.ServletHealthResponderFactory.SERVLET_SUFFIX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServletHealthResponderFactoryTest {
    private static final String NAME = "tests";
    private static final String HEALTH_CHECK_URI = "/health-check";
    private static final HealthResponse SUCCESS = new HealthResponse(true, "healthy", MediaType.TEXT_PLAIN,
        HttpServletResponse.SC_OK);
    private static final HealthResponse FAIL = new HealthResponse(false, "unhealthy", MediaType.TEXT_PLAIN,
        HttpServletResponse.SC_SERVICE_UNAVAILABLE);

    private final ObjectMapper mapper = Jackson.newObjectMapper();
    private final Validator validator = Validators.newValidator();
    private final YamlConfigurationFactory<HealthResponderFactory> configFactory =
        new YamlConfigurationFactory<>(HealthResponderFactory.class, validator, mapper, "dw");
    private final HttpTester.Request request = new HttpTester.Request();

    private ServletTester servletTester;
    private ArgumentCaptor<ServletHealthResponder> servletCaptor;

    @Mock
    private HealthResponseProvider healthResponseProvider;
    @Mock
    private ServletEnvironment servlets;
    @Mock
    private JerseyEnvironment jersey;
    @Mock
    private ServletRegistration.Dynamic servletRegistration;
    @Mock
    private HealthEnvironment health;

    @BeforeEach
    void setUp() {
        servletTester = new ServletTester();
        servletCaptor = ArgumentCaptor.forClass(ServletHealthResponder.class);

        request.setHeader(HttpHeader.HOST.asString(), "localhost");
        request.setURI(HEALTH_CHECK_URI);
        request.setMethod(HttpMethod.GET.asString());
    }

    @AfterEach
    void tearDown() throws Exception {
        servletTester.stop();
    }

    @Test
    void isDiscoverable() {
        // given
        DiscoverableSubtypeResolver resolver = new DiscoverableSubtypeResolver();

        // when
        List<Class<?>> subtypes = resolver.getDiscoveredSubtypes();

        // then
        assertThat(subtypes).contains(ServletHealthResponderFactory.class);
    }

    @Test
    void testBuildHealthServlet() throws Exception {
        // given
        HealthResponderFactory factory = configFactory.build(new ResourceConfigurationSourceProvider(), "/yml/servlet-responder-factory-caching.yml");
        setupServletStubbing();

        // when
        // succeed first, fail second
        when(healthResponseProvider.healthResponse(Collections.emptyMap())).thenReturn(SUCCESS, FAIL);
        factory.configure(NAME, Collections.singletonList(HEALTH_CHECK_URI), healthResponseProvider, health, jersey,
            servlets, mapper);
        servletTester.addServlet(new ServletHolder(servletCaptor.getValue()), HEALTH_CHECK_URI);
        servletTester.start();
        HttpTester.Response healthyResponse = executeRequest(request);
        HttpTester.Response unhealthyResponse = executeRequest(request);

        // then
        assertThat(healthyResponse.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        assertThat(unhealthyResponse.getStatus()).isEqualTo(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
    }

    @Test
    void testBuildHealthServletWithCacheControlDisabled() throws Exception {
        // given
        HealthResponderFactory factory = configFactory.build(new ResourceConfigurationSourceProvider(), "/yml/servlet-responder-factory-caching-header-disabled.yml");
        setupServletStubbing();

        // when
        // succeed first, fail second
        when(healthResponseProvider.healthResponse(Collections.emptyMap())).thenReturn(SUCCESS, FAIL);
        factory.configure(NAME, Collections.singletonList(HEALTH_CHECK_URI), healthResponseProvider, health, jersey,
            servlets, mapper);
        servletTester.addServlet(new ServletHolder(servletCaptor.getValue()), HEALTH_CHECK_URI);
        servletTester.start();
        HttpTester.Response healthyResponse = executeRequest(request);
        HttpTester.Response unhealthyResponse = executeRequest(request);

        // then
        assertThat(healthyResponse.getStatus()).isEqualTo(HttpServletResponse.SC_OK);
        assertThat(healthyResponse.get(HttpHeader.CACHE_CONTROL)).isNull();
        assertThat(unhealthyResponse.getStatus()).isEqualTo(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
        assertThat(unhealthyResponse.get(HttpHeader.CACHE_CONTROL)).isNull();
    }

    private HttpTester.Response executeRequest(HttpTester.Request request) throws Exception {
        return HttpTester.parseResponse(servletTester.getResponses(request.generate()));
    }

    private void setupServletStubbing() {
        when(servlets.addServlet(eq(NAME + SERVLET_SUFFIX), servletCaptor.capture()))
            .thenReturn(servletRegistration);
        when(servletRegistration.addMapping(HEALTH_CHECK_URI))
            .thenReturn(Collections.singleton(HEALTH_CHECK_URI));
    }
}
