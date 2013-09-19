package io.dropwizard.jetty;

import com.google.common.collect.ImmutableMap;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.*;

public class ContextRoutingHandlerTest {
    private final Request baseRequest = mock(Request.class);
    private final HttpServletRequest request = mock(HttpServletRequest.class);
    private final HttpServletResponse response = mock(HttpServletResponse.class);

    private final Handler handler1 = mock(Handler.class);
    private final Handler handler2 = mock(Handler.class);

    private ContextRoutingHandler handler;

    @Before
    public void setUp() throws Exception {
        this.handler = new ContextRoutingHandler(ImmutableMap.of(
                "/", handler1,
                "/admin", handler2
        ));
    }

    @Test
    public void routesToTheBestPrefixMatch() throws Exception {
        when(baseRequest.getRequestURI()).thenReturn("/hello-world");

        handler.handle("/hello-world", baseRequest, request, response);

        verify(handler1).handle("/hello-world", baseRequest, request, response);
        verify(handler2, never()).handle("/hello-world", baseRequest, request, response);
    }

    @Test
    public void routesToTheLongestPrefixMatch() throws Exception {
        when(baseRequest.getRequestURI()).thenReturn("/admin/woo");

        handler.handle("/admin/woo", baseRequest, request, response);

        verify(handler1, never()).handle("/admin/woo", baseRequest, request, response);
        verify(handler2).handle("/admin/woo", baseRequest, request, response);
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
        handler.stop();

        final InOrder inOrder = inOrder(handler1, handler2);
        inOrder.verify(handler1).start();
        inOrder.verify(handler2).start();
    }
}
