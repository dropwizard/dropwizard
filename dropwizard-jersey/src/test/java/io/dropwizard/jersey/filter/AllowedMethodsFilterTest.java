package io.dropwizard.jersey.filter;

import com.google.common.collect.ImmutableMap;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.WebAppDescriptor;
import io.dropwizard.logging.LoggingFactory;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.*;

public class AllowedMethodsFilterTest extends JerseyTest {
    static {
        LoggingFactory.bootstrap();
    }

    private static final int DISALLOWED_STATUS_CODE = ClientResponse.Status.METHOD_NOT_ALLOWED.getStatusCode();
    private static final int OK_STATUS_CODE = ClientResponse.Status.OK.getStatusCode();

    private final HttpServletRequest request = mock(HttpServletRequest.class);
    private final HttpServletResponse response = mock(HttpServletResponse.class);
    private final FilterChain chain = mock(FilterChain.class);
    private final FilterConfig config = mock(FilterConfig.class);
    private final AllowedMethodsFilter filter = new AllowedMethodsFilter();

    @Before
    public void setUp() {
        filter.init(config);
    }

    @Override
    protected AppDescriptor configure() {
        return new WebAppDescriptor.Builder("io.dropwizard.jersey.filter")
                .addFilter(AllowedMethodsFilter.class, "allowedMethods", ImmutableMap.of(AllowedMethodsFilter.ALLOWED_METHODS_PARAM, "GET,POST"))
                .build();
    }

    private int getResponseStatusForRequestMethod(String method) {
        final ClientResponse response = resource().path("/ping").method(method, ClientResponse.class);

        try {
            return response.getStatus();
        }
        finally {
            response.close();
        }
    }

    @Test
    public void testGetRequestAllowed() {
        assertEquals(OK_STATUS_CODE, getResponseStatusForRequestMethod("GET"));
    }

    @Test
    public void testPostRequestAllowed() {
        assertEquals(OK_STATUS_CODE, getResponseStatusForRequestMethod("POST"));
    }

    @Test
    public void testPutRequestBlocked() {
        assertEquals(DISALLOWED_STATUS_CODE, getResponseStatusForRequestMethod("PUT"));
    }

    @Test
    public void testDeleteRequestBlocked() {
        assertEquals(DISALLOWED_STATUS_CODE, getResponseStatusForRequestMethod("DELETE"));
    }

    @Test
    public void testTraceRequestBlocked() {
        assertEquals(DISALLOWED_STATUS_CODE, getResponseStatusForRequestMethod("TRACE"));
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
