package io.dropwizard.jersey.filter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import io.dropwizard.jersey.AbstractJerseyTest;
import io.dropwizard.jersey.DropwizardResourceConfig;
import java.io.IOException;
import java.util.Collections;
import java.util.Map;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
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
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new GrizzlyWebTestContainerFactory();
    }

    @Override
    protected DeploymentContext configureDeployment() {
        final ResourceConfig rc = DropwizardResourceConfig.forTesting();

        final Map<String, String> filterParams =
                Collections.singletonMap(AllowedMethodsFilter.ALLOWED_METHODS_PARAM, "GET,POST");

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
        assertEquals(OK_STATUS_CODE, getResponseStatusForRequestMethod("GET", false));
    }

    @Test
    void testPostRequestAllowed() {
        assertEquals(OK_STATUS_CODE, getResponseStatusForRequestMethod("POST", true));
    }

    @Test
    void testPutRequestBlocked() {
        assertEquals(DISALLOWED_STATUS_CODE, getResponseStatusForRequestMethod("PUT", true));
    }

    @Test
    void testDeleteRequestBlocked() {
        assertEquals(DISALLOWED_STATUS_CODE, getResponseStatusForRequestMethod("DELETE", false));
    }

    @Test
    void testTraceRequestBlocked() {
        assertEquals(DISALLOWED_STATUS_CODE, getResponseStatusForRequestMethod("TRACE", false));
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
