package io.dropwizard.jetty;

import com.google.common.collect.ImmutableMap;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.junit.Test;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class RoutingHandlerTest {
    private final Connector connector1 = mock(Connector.class);
    private final Connector connector2 = mock(Connector.class);
    private final Handler handler1 = spy(new ContextHandler());
    private final Handler handler2 = spy(new ContextHandler());

    private final RoutingHandler handler = new RoutingHandler(ImmutableMap.of(connector1,
                                                                              handler1,
                                                                              connector2,
                                                                              handler2));

    @Test
    public void startsAndStopsAllHandlers() throws Exception {
        handler1.setServer(mock(Server.class));
        handler2.setServer(mock(Server.class));
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

    @Test
    @SuppressWarnings({ "rawtypes", "unchecked" })
    public void routesRequestsToTheConnectorSpecificHandler() throws Exception {
        final HttpChannel channel = mock(HttpChannel.class);
        when(channel.getConnector()).thenReturn(connector1);

        final Request baseRequest = mock(Request.class);
        when(baseRequest.getHttpChannel()).thenReturn(channel);

        final HttpServletRequest request = mock(HttpServletRequest.class);
        final HttpServletResponse response = mock(HttpServletResponse.class);

        handler.handle("target", baseRequest, request, response);

        verify(handler1).handle("target", baseRequest, request, response);
    }
}
