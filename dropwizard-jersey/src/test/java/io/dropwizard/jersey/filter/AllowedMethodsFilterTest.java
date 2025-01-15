package io.dropwizard.jersey.filter;

import io.dropwizard.jersey.AbstractJerseyTest;
import io.dropwizard.jersey.DropwizardResourceConfig;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletProperties;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class AllowedMethodsFilterTest extends AbstractJerseyTest {

    private static final int DISALLOWED_STATUS_CODE = Response.Status.METHOD_NOT_ALLOWED.getStatusCode();
    private static final int OK_STATUS_CODE = Response.Status.OK.getStatusCode();

    private final HttpServletRequest request = mock(HttpServletRequest.class);
    private final HttpServletResponse response = mock(HttpServletResponse.class);
    private final FilterChain chain = mock(FilterChain.class);
    private final FilterConfig config = mock(FilterConfig.class);
    private final AllowedMethodsFilter filter = new AllowedMethodsFilter();

    @BeforeEach
    void setUpFilter() {
        filter.init(config);
    }


    @Override
    protected TestContainerFactory getTestContainerFactory()
            throws TestContainerException {
        return new GrizzlyWebTestContainerFactory();
    }

    @Override
    protected DeploymentContext configureDeployment() {
        final ResourceConfig rc = DropwizardResourceConfig.forTesting();

        final Map<String, String> filterParams = Collections.singletonMap(AllowedMethodsFilter.ALLOWED_METHODS_PARAM, "GET,POST");

        return ServletDeploymentContext.builder(rc)
                .addFilter(AllowedMethodsFilter.class, "allowedMethodsFilter", filterParams)
                .initParam(ServletProperties.JAXRS_APPLICATION_CLASS, DropwizardResourceConfig.class.getName())
                .initParam(ServerProperties.PROVIDER_CLASSNAMES, DummyResource.class.getName())
                .build();
    }

    private int getResponseStatusForRequestMethod(String method, boolean includeEntity) {
        try (Response resourceResponse = includeEntity
            ? target("/ping").request().method(method, Entity.entity("", MediaType.TEXT_PLAIN))
            : target("/ping").request().method(method)) {
            return resourceResponse.getStatus();
        }
    }

    @Test
    void testGetRequestAllowed() {
        assertThat(getResponseStatusForRequestMethod("GET", false))
            .isEqualTo(OK_STATUS_CODE);
    }

    @Test
    void testPostRequestAllowed() {
        assertThat(getResponseStatusForRequestMethod("POST", true))
            .isEqualTo(OK_STATUS_CODE);
    }

    @Test
    void testPutRequestBlocked() {
        assertThat(getResponseStatusForRequestMethod("PUT", true))
            .isEqualTo(DISALLOWED_STATUS_CODE);
    }

    @Test
    void testDeleteRequestBlocked() {
        assertThat(getResponseStatusForRequestMethod("DELETE", false))
            .isEqualTo(DISALLOWED_STATUS_CODE);
    }

    @Test
    void testTraceRequestBlocked() {
        assertThat(getResponseStatusForRequestMethod("TRACE", false))
            .isEqualTo(DISALLOWED_STATUS_CODE);
    }

    @Test
    void allowsAllowedMethod() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    void blocksDisallowedMethod() throws Exception {
        when(request.getMethod()).thenReturn("TRACE");
        filter.doFilter(request, response, chain);

        verify(chain, never()).doFilter(request, response);
    }

    @Test
    void disallowedMethodCausesMethodNotAllowedResponse() throws IOException, ServletException {
        when(request.getMethod()).thenReturn("TRACE");
        filter.doFilter(request, response, chain);
        verify(response).sendError(DISALLOWED_STATUS_CODE);
    }
}
