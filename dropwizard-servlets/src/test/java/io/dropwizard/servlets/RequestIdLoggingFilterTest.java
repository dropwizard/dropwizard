package io.dropwizard.servlets;

import io.dropwizard.util.RequestId;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.slf4j.MDC;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class RequestIdLoggingFilterTest {
    private final HttpServletRequest request = mock(HttpServletRequest.class);
    private final HttpServletResponse response = mock(HttpServletResponse.class);
    private final RequestIdLoggingFilter filter = new RequestIdLoggingFilter("ISO8859-1", "ClientHeader", "ServiceHeader");

    @Test
    public void hasClientRequestId() throws Exception {
        String clientId = UUID.randomUUID().toString();
        FilterChainStub chain = new FilterChainStub(clientId);
        ArgumentCaptor<String> serviceIdHeader = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<String> clientIdHeader = ArgumentCaptor.forClass(String.class);

        when(request.getHeader("ClientHeader")).thenReturn(clientId);
        filter.doFilter(request, response, chain);

        verify(response).addHeader(eq("ClientHeader"), clientIdHeader.capture());
        verify(response).addHeader(eq("ServiceHeader"), serviceIdHeader.capture());
        verifyNoMoreInteractions(response);

        assertEquals(clientId, clientIdHeader.getValue());
        Long.valueOf(serviceIdHeader.getValue());
        assertTrue(chain.wasInvoked);
    }

    @Test
    public void noClientRequestId() throws Exception {
        FilterChainStub chain = new FilterChainStub(null);
        ArgumentCaptor<String> serviceIdHeader = ArgumentCaptor.forClass(String.class);

        when(request.getHeader("ClientHeader")).thenReturn(null);
        filter.doFilter(request, response, chain);

        verify(response).addHeader(eq("ServiceHeader"), serviceIdHeader.capture());
        verifyNoMoreInteractions(response);

        Long.valueOf(serviceIdHeader.getValue());
        assertTrue(chain.wasInvoked);
    }

    @Test
    public void invalidClientRequestId() throws Exception {
        FilterChainStub chain = new FilterChainStub(null);
        ArgumentCaptor<String> serviceIdHeader = ArgumentCaptor.forClass(String.class);

        when(request.getHeader("ClientHeader")).thenReturn("*");
        filter.doFilter(request, response, chain);

        verify(response).addHeader(eq("ServiceHeader"), serviceIdHeader.capture());
        verifyNoMoreInteractions(response);

        Long.valueOf(serviceIdHeader.getValue());
        assertTrue(chain.wasInvoked);
    }

    @Test
    public void mdcIsCleanedUp() throws Exception {
        assertTrue(MDC.getCopyOfContextMap() == null || MDC.getCopyOfContextMap().isEmpty());
        filter.doFilter(request, response, mock(FilterChain.class));
        assertTrue(MDC.getCopyOfContextMap().isEmpty());
    }

    private class FilterChainStub implements FilterChain {
        private final String expectedClientRequestId;
        boolean wasInvoked = false;

        private FilterChainStub(String expectedClientRequestId) {
            this.expectedClientRequestId = expectedClientRequestId;
        }

        @Override
        public void doFilter(ServletRequest request, ServletResponse response) throws IOException, ServletException {
            assertEquals(expectedClientRequestId, MDC.get(RequestId.CLIENT_REQUEST_ID));
            Long.valueOf(MDC.get(RequestId.SERVICE_REQUEST_ID));

            wasInvoked = true;
        }
    }
}
