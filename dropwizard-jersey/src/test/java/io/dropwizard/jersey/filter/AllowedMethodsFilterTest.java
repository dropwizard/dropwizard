package io.dropwizard.jersey.filter;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.logging.BootstrapLogging;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletProperties;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AllowedMethodsFilterTest extends JerseyTest {
    static {
        BootstrapLogging.bootstrap();
    }

    private static final int DISALLOWED_STATUS_CODE = Response.Status.METHOD_NOT_ALLOWED.getStatusCode();
    private static final int OK_STATUS_CODE = Response.Status.OK.getStatusCode();

    private final HttpServletRequest request = mock(HttpServletRequest.class);
    private final HttpServletResponse response = mock(HttpServletResponse.class);
    private final FilterChain chain = mock(FilterChain.class);
    private final FilterConfig config = mock(FilterConfig.class);
    private final AllowedMethodsFilter filter = new AllowedMethodsFilter();

    @Before
    public void setUpFilter() {
        filter.init(config);
    }


    @Override
    protected TestContainerFactory getTestContainerFactory()
            throws TestContainerException {
        return new GrizzlyWebTestContainerFactory();
    }

    @Override
    protected DeploymentContext configureDeployment() {
        forceSet(TestProperties.CONTAINER_PORT, "0");
        final ResourceConfig rc = DropwizardResourceConfig.forTesting(new MetricRegistry());

        final Map<String, String> filterParams = ImmutableMap.of(
                AllowedMethodsFilter.ALLOWED_METHODS_PARAM, "GET,POST");

        return ServletDeploymentContext.builder(rc)
                .addFilter(AllowedMethodsFilter.class, "allowedMethodsFilter", filterParams)
                .initParam(ServletProperties.JAXRS_APPLICATION_CLASS, DropwizardResourceConfig.class.getName())
                .initParam(ServerProperties.PROVIDER_CLASSNAMES, DummyResource.class.getName())
                .build();
    }

    private int getResponseStatusForRequestMethod(String method, boolean includeEntity) {
        final Response resourceResponse = includeEntity
                ? target("/ping").request().method(method, Entity.entity("", MediaType.TEXT_PLAIN))
                : target("/ping").request().method(method);

        try {
            return resourceResponse.getStatus();
        } finally {
            resourceResponse.close();
        }
    }

    @Test
    public void testGetRequestAllowed() {
        assertEquals(OK_STATUS_CODE, getResponseStatusForRequestMethod("GET", false));
    }

    @Test
    public void testPostRequestAllowed() {
        assertEquals(OK_STATUS_CODE, getResponseStatusForRequestMethod("POST", true));
    }

    @Test
    public void testPutRequestBlocked() {
        assertEquals(DISALLOWED_STATUS_CODE, getResponseStatusForRequestMethod("PUT", true));
    }

    @Test
    public void testDeleteRequestBlocked() {
        assertEquals(DISALLOWED_STATUS_CODE, getResponseStatusForRequestMethod("DELETE", false));
    }

    @Test
    public void testTraceRequestBlocked() {
        assertEquals(DISALLOWED_STATUS_CODE, getResponseStatusForRequestMethod("TRACE", false));
    }

    @Test
    public void allowsAllowedMethod() throws Exception {
        when(request.getMethod()).thenReturn("GET");
        filter.doFilter(request, response, chain);

        verify(chain).doFilter(request, response);
    }

    @Test
    public void blocksDisallowedMethod() throws Exception {
        when(request.getMethod()).thenReturn("TRACE");
        filter.doFilter(request, response, chain);

        verify(chain, never()).doFilter(request, response);
    }

    @Test
    public void disallowedMethodCausesMethodNotAllowedResponse() throws IOException, ServletException {
        when(request.getMethod()).thenReturn("TRACE");
        filter.doFilter(request, response, chain);
        verify(response).sendError(eq(DISALLOWED_STATUS_CODE));
    }
}
