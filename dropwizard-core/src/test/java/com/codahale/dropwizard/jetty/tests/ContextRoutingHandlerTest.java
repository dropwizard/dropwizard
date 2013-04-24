package com.codahale.dropwizard.jetty.tests;

import com.codahale.dropwizard.jetty.ContextRoutingHandler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class ContextRoutingHandlerTest {
    private final Request baseRequest = mock(Request.class);
    private final HttpServletRequest request = mock(HttpServletRequest.class);
    private final HttpServletResponse response = mock(HttpServletResponse.class);

    private final ContextHandler handler1 = spy(new ContextHandler());
    private final ContextHandler handler2 = spy(new ContextHandler());

    private ContextRoutingHandler handler;

    @Before
    public void setUp() throws Exception {
        handler1.setContextPath("/");
        handler2.setContextPath("/admin");

        this.handler = new ContextRoutingHandler(handler1, handler2);
    }

    @Test
    public void routesToTheBestPrefixMatch() throws Exception {
        when(baseRequest.getRequestURI()).thenReturn("/hello-world");

        handler.handle("/hello-world", baseRequest, request, response);

        verify(handler1).handle("/hello-world", baseRequest, request, response);
        verify(handler2, never()).handle("/hello-world", baseRequest, request, response);
    }

    @Test
    public void passesHandlingNonMatchingRequests() throws Exception {
        when(baseRequest.getRequestURI()).thenReturn("WAT");

        handler.handle("WAT", baseRequest, request, response);

        verify(handler1, never()).handle("WAT", baseRequest, request, response);
        verify(handler2, never()).handle("WAT", baseRequest, request, response);
    }

    @Test
    public void startsAndStopsAllHandlers() throws Exception {
        handler.start();
        try {
            assertThat(handler1.isStarted())
                    .isTrue();
            assertThat(handler2.isStarted())
                    .isTrue();
        } finally {
            handler.stop();
        }

        assertThat(handler1.isStopped())
                .isTrue();
        assertThat(handler2.isStopped())
                .isTrue();
    }
}
