package io.dropwizard.jetty;

import org.eclipse.jetty.http.HttpURI;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.util.Callback;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Map;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ContextRoutingHandlerTest {
    @Mock
    private Request request;
    @Mock
    private Response response;
    @Mock
    private Callback callback;

    @Mock
    private Handler handler1;
    @Mock
    private Handler handler2;

    private ContextRoutingHandler handler;

    @BeforeEach
    void setUp() {
        this.handler = new ContextRoutingHandler(Map.of(
                "/", handler1,
                "/admin", handler2
        ));
    }

    @Test
    void routesToTheBestPrefixMatch() throws Exception {
        HttpURI httpURI = mock(HttpURI.class);
        when(httpURI.getPath()).thenReturn("/hello-world");
        when(request.getHttpURI()).thenReturn(httpURI);

        handler.handle(request, response, callback);

        verify(handler1).handle(request, response, callback);
        verify(handler2, never()).handle(request, response, callback);
    }

    @Test
    void routesToTheLongestPrefixMatch() throws Exception {
        HttpURI httpURI = mock(HttpURI.class);
        when(httpURI.getPath()).thenReturn("/admin/woo");
        when(request.getHttpURI()).thenReturn(httpURI);

        handler.handle(request, response, callback);

        verify(handler1, never()).handle(request, response, callback);
        verify(handler2).handle(request, response, callback);
    }

    @Test
    void passesHandlingNonMatchingRequests() throws Exception {
        HttpURI httpURI = mock(HttpURI.class);
        when(httpURI.getPath()).thenReturn("WAT");
        when(request.getHttpURI()).thenReturn(httpURI);

        handler.handle(request, response, callback);

        verify(handler1, never()).handle(request, response, callback);
        verify(handler2, never()).handle(request, response, callback);
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
