package io.dropwizard.jetty;

import com.google.common.collect.ImmutableMap;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.junit.Before;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;

public class ContextRoutingHandlerTest {
    private Request baseRequest = mock(Request.class);
    private HttpServletRequest request = mock(HttpServletRequest.class);
    private HttpServletResponse response = mock(HttpServletResponse.class);
    private Handler handler1 = mock(Handler.class);
    private Handler handler2 = mock(Handler.class);

    private ContextRoutingHandler handler;

    @Before
    public void setUp() throws Exception {
        openMocks(this);
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
        verify(handler1).start();
        verify(handler2).start();

        handler.stop();
        verify(handler1).stop();
        verify(handler2).stop();
    }
}
