package io.dropwizard.health.response;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.io.Resources;
import io.dropwizard.configuration.YamlConfigurationFactory;
import io.dropwizard.health.HealthStatusChecker;
import io.dropwizard.jackson.DiscoverableSubtypeResolver;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.jersey.validation.Validators;
import java.io.File;
import java.io.IOException;
import java.util.List;
import javax.servlet.http.HttpServlet;
import javax.validation.Validator;
import javax.ws.rs.core.MediaType;
import org.eclipse.jetty.http.HttpHeader;
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
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class DefaultHealthServletFactoryTest {
    private static final String HEALTH_CHECK_URI = "/health-check";
    private static final String PLAIN_TEXT_UTF_8 = "text/plain;charset=UTF-8";
    private final ObjectMapper mapper = Jackson.newObjectMapper();
    private final Validator validator = Validators.newValidator();
    private final YamlConfigurationFactory<HealthServletFactory> configFactory =
            new YamlConfigurationFactory<>(HealthServletFactory.class, validator, mapper, "dw");
    private final HttpTester.Request request = new HttpTester.Request();

    private ServletTester servletTester;

    @Mock
    private HealthStatusChecker healthStatusChecker;

    @BeforeEach
    public void setUp() throws Exception {
        servletTester = new ServletTester();

        request.setHeader(HttpHeader.HOST.asString(), "localhost");
        request.setURI(HEALTH_CHECK_URI);
        request.setMethod("GET");
    }

    @AfterEach
    public void tearDown() throws Exception {
        servletTester.stop();
    }

    @Test
    public void isDiscoverable() {
        // given
        DiscoverableSubtypeResolver resolver = new DiscoverableSubtypeResolver();

        // when
        List<Class<?>> subtypes = resolver.getDiscoveredSubtypes();

        // then
        assertThat(subtypes).contains(DefaultHealthServletFactory.class);
    }

    @Test
    public void testBuildHealthServlet() throws Exception {
        // given
        File yml = new File(Resources.getResource("yml/servlet-factory-caching.yml").toURI());

        // when
        // succeed first, fail second
        when(healthStatusChecker.isHealthy(isNull())).thenReturn(true, false);
        HealthServletFactory factory = configFactory.build(yml);
        HttpServlet servlet = factory.build(healthStatusChecker);
        servletTester.addServlet(new ServletHolder(servlet), HEALTH_CHECK_URI);
        servletTester.start();
        HttpTester.Response healthyResponse = executeRequest(request);
        HttpTester.Response unhealthyResponse = executeRequest(request);

        // then
        assertThat(healthyResponse.getStatus()).isEqualTo(Response.SC_OK);
        assertThat(healthyResponse.get(HttpHeader.CONTENT_TYPE)).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(parseResponseBody(healthyResponse)).isEqualTo("healthy");
        assertThat(unhealthyResponse.getStatus()).isEqualTo(Response.SC_SERVICE_UNAVAILABLE);
        assertThat(unhealthyResponse.get(HttpHeader.CONTENT_TYPE)).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(parseResponseBody(unhealthyResponse)).isEqualTo("unhealthy");
    }

    @Test
    public void testBuildHealthServletWithCacheControlDisabled() throws Exception {
        // given
        File yml = new File(Resources.getResource("yml/servlet-factory-caching-header-disabled.yml").toURI());

        // when
        // succeed first, fail second
        when(healthStatusChecker.isHealthy(isNull())).thenReturn(true, false);
        HealthServletFactory factory = configFactory.build(yml);
        HttpServlet servlet = factory.build(healthStatusChecker);
        servletTester.addServlet(new ServletHolder(servlet), HEALTH_CHECK_URI);
        servletTester.start();
        HttpTester.Response healthyResponse = executeRequest(request);
        HttpTester.Response unhealthyResponse = executeRequest(request);

        // then
        assertThat(healthyResponse.getStatus()).isEqualTo(Response.SC_OK);
        assertThat(healthyResponse.get(HttpHeader.CONTENT_TYPE)).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(parseResponseBody(healthyResponse)).isEqualTo("healthy");
        assertThat(healthyResponse.get(HttpHeader.CACHE_CONTROL)).isNull();
        assertThat(unhealthyResponse.getStatus()).isEqualTo(Response.SC_SERVICE_UNAVAILABLE);
        assertThat(unhealthyResponse.get(HttpHeader.CONTENT_TYPE)).isEqualTo(MediaType.APPLICATION_JSON);
        assertThat(parseResponseBody(unhealthyResponse)).isEqualTo("unhealthy");
        assertThat(unhealthyResponse.get(HttpHeader.CACHE_CONTROL)).isNull();
    }

    @Test
    public void testBuildHealthServletWithCustomResponses() throws Exception {
        // given
        File yml = new File(Resources.getResource("yml/servlet-factory-custom-responses.yml").toURI());

        // when
        // succeed first, fail second
        when(healthStatusChecker.isHealthy(isNull())).thenReturn(true, false);
        HealthServletFactory factory = configFactory.build(yml);
        HttpServlet servlet = factory.build(healthStatusChecker);
        servletTester.addServlet(new ServletHolder(servlet), HEALTH_CHECK_URI);
        servletTester.start();
        HttpTester.Response healthyResponse = executeRequest(request);
        HttpTester.Response unhealthyResponse = executeRequest(request);

        // then
        assertThat(healthyResponse.getStatus()).isEqualTo(Response.SC_OK);
        assertThat(healthyResponse.get(HttpHeader.CONTENT_TYPE)).isEqualTo(PLAIN_TEXT_UTF_8);
        assertThat(healthyResponse.getContent()).isEqualTo("HAPPY");
        assertThat(unhealthyResponse.getStatus()).isEqualTo(Response.SC_SERVICE_UNAVAILABLE);
        assertThat(unhealthyResponse.get(HttpHeader.CONTENT_TYPE)).isEqualTo(PLAIN_TEXT_UTF_8);
        assertThat(unhealthyResponse.getContent()).isEqualTo("SAD");
    }

    private HttpTester.Response executeRequest(HttpTester.Request request) throws Exception {
        return HttpTester.parseResponse(servletTester.getResponses(request.generate()));
    }

    private String parseResponseBody(HttpTester.Response response) throws IOException {
        JsonNode jsonBody = mapper.readValue(response.getContentBytes(), JsonNode.class);
        return jsonBody.get("status").asText();
    }
}
