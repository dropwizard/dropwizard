package io.dropwizard.health.response;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.health.HealthEnvironment;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.jersey.validation.Validators;
import io.dropwizard.jetty.setup.ServletEnvironment;
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
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.ServletRegistration;
import javax.servlet.http.HttpServlet;
import javax.validation.Validator;
import javax.ws.rs.core.MediaType;
import java.io.File;
import java.util.Collections;
import java.util.List;

import static io.dropwizard.health.response.ServletHealthResponderFactory.SERVLET_SUFFIX;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ServletHealthResponderFactoryTest {
    private static final String NAME = "tests";
    private static final String HEALTH_CHECK_URI = "/health-check";
    private static final HealthResponse SUCCESS = new HealthResponse(true, "healthy", MediaType.TEXT_PLAIN);
    private static final HealthResponse FAIL = new HealthResponse(false, "unhealthy", MediaType.TEXT_PLAIN);

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
    void setUp() throws Exception {
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
        File yml = new File(Resources.getResource("yml/servlet-responder-factory-caching.yml").toURI());
        setupServletStubbing();

        // when
        // succeed first, fail second
        when(healthResponseProvider.minimalHealthResponse(isNull())).thenReturn(SUCCESS, FAIL);
        HealthResponderFactory factory = configFactory.build(yml);
        factory.configure(NAME, Collections.singletonList(HEALTH_CHECK_URI), healthResponseProvider, health, jersey,
            servlets, mapper);
        servletTester.addServlet(new ServletHolder(servletCaptor.getValue()), HEALTH_CHECK_URI);
        servletTester.start();
        HttpTester.Response healthyResponse = executeRequest(request);
        HttpTester.Response unhealthyResponse = executeRequest(request);

        // then
        assertThat(healthyResponse.getStatus()).isEqualTo(Response.SC_OK);
        assertThat(unhealthyResponse.getStatus()).isEqualTo(Response.SC_SERVICE_UNAVAILABLE);
        verify(health).setHealthResponder(servletCaptor.getValue());
    }

    @Test
    void testBuildHealthServletWithCacheControlDisabled() throws Exception {
        // given
        File yml = new File(Resources.getResource("yml/servlet-responder-factory-caching-header-disabled.yml").toURI());
        setupServletStubbing();

        // when
        // succeed first, fail second
        when(healthResponseProvider.minimalHealthResponse(isNull())).thenReturn(SUCCESS, FAIL);
        HealthResponderFactory factory = configFactory.build(yml);
        factory.configure(NAME, Collections.singletonList(HEALTH_CHECK_URI), healthResponseProvider, health, jersey,
            servlets, mapper);
        servletTester.addServlet(new ServletHolder(servletCaptor.getValue()), HEALTH_CHECK_URI);
        servletTester.start();
        HttpTester.Response healthyResponse = executeRequest(request);
        HttpTester.Response unhealthyResponse = executeRequest(request);

        // then
        assertThat(healthyResponse.getStatus()).isEqualTo(Response.SC_OK);
        assertThat(healthyResponse.get(HttpHeader.CACHE_CONTROL)).isNull();
        assertThat(unhealthyResponse.getStatus()).isEqualTo(Response.SC_SERVICE_UNAVAILABLE);
        assertThat(unhealthyResponse.get(HttpHeader.CACHE_CONTROL)).isNull();
        verify(health).setHealthResponder(servletCaptor.getValue());
    }

    private HttpTester.Response executeRequest(HttpTester.Request request) throws Exception {
        return HttpTester.parseResponse(servletTester.getResponses(request.generate()));
    }

    private void setupServletStubbing() {
        when(servlets.addServlet(eq(NAME + SERVLET_SUFFIX), servletCaptor.capture()))
            .thenReturn(servletRegistration);
        when(servletRegistration.addMapping(eq(HEALTH_CHECK_URI)))
            .thenReturn(Collections.singleton(HEALTH_CHECK_URI));
    }
}
