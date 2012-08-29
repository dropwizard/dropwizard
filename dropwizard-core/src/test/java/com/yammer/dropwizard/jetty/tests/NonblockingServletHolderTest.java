package com.yammer.dropwizard.jetty.tests;

import com.yammer.dropwizard.jetty.NonblockingServletHolder;
import org.eclipse.jetty.server.Request;
import org.junit.Test;
import org.mockito.InOrder;

import javax.servlet.Servlet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class NonblockingServletHolderTest {
    private final Servlet servlet = mock(Servlet.class);
    private final NonblockingServletHolder holder = new NonblockingServletHolder(servlet);
    private final Request baseRequest = mock(Request.class);
    private final ServletRequest request = mock(ServletRequest.class);
    private final ServletResponse response = mock(ServletResponse.class);

    @Test
    public void hasAServlet() throws Exception {
        assertThat(holder.getServlet())
                .isEqualTo(servlet);
    }

    @Test
    public void servicesRequests() throws Exception {
        holder.handle(baseRequest, request, response);

        verify(servlet).service(request, response);
    }

    @Test
    public void temporarilyDisablesAsyncRequestsIfDisabled() throws Exception {
        holder.setAsyncSupported(false);

        holder.handle(baseRequest, request, response);

        final InOrder inOrder = inOrder(baseRequest, servlet);

        inOrder.verify(baseRequest).setAsyncSupported(false);
        inOrder.verify(servlet).service(request, response);
    }
}
