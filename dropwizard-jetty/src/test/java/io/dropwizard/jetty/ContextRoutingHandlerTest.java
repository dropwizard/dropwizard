package io.dropwizard.jetty;

import io.dropwizard.util.Maps;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContextRoutingHandlerTest {
    @Mock
    private Request baseRequest;
    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpServletResponse response;

    @Mock
    private Handler handler1;
    @Mock
    private Handler handler2;

    private ContextRoutingHandler handler;

    @BeforeEach
    void setUp() {
        this.handler = new ContextRoutingHandler(Maps.of(
                "/", handler1,
                "/admin", handler2
        ));
    }

    @Test
    void routesToTheBestPrefixMatch() throws Exception {
        when(baseRequest.getRequestURI()).thenReturn("/hello-world");

        handler.handle("/hello-world", baseRequest, request, response);

        verify(handler1).handle("/hello-world", baseRequest, request, response);
        verify(handler2, never()).handle("/hello-world", baseRequest, request, response);
    }

    @Test
    void routesToTheLongestPrefixMatch() throws Exception {
        when(baseRequest.getRequestURI()).thenReturn("/admin/woo");

        handler.handle("/admin/woo", baseRequest, request, response);

        verify(handler1, never()).handle("/admin/woo", baseRequest, request, response);
        verify(handler2).handle("/admin/woo", baseRequest, request, response);
    }

    @Test
    void passesHandlingNonMatchingRequests() throws Exception {
        when(baseRequest.getRequestURI()).thenReturn("WAT");

        handler.handle("WAT", baseRequest, request, response);

        verify(handler1, never()).handle("WAT", baseRequest, request, response);
        verify(handler2, never()).handle("WAT", baseRequest, request, response);
    }

    @Test
    void startsAndStopsAllHandlers() throws Exception {
        handler.start();
        verify(handler1).start();
        verify(handler2).start();

        handler.stop();
        verify(handler1).stop();
        verify(handler2).stop();
    }
}
