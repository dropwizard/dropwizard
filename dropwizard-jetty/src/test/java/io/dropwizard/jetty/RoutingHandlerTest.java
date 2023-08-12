package io.dropwizard.jetty;

import org.eclipse.jetty.ee10.servlet.ServletContextHandler;
import org.eclipse.jetty.ee10.servlet.SessionHandler;
import org.eclipse.jetty.server.ConnectionMetaData;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Response;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.ContextHandler;
import org.eclipse.jetty.util.Callback;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class RoutingHandlerTest {
    private final Connector connector1 = mock(Connector.class);
    private final Connector connector2 = mock(Connector.class);
    private final Handler handler1 = spy(new ContextHandler());
    private final Handler handler2 = spy(new ContextHandler());

    private final RoutingHandler handler = new RoutingHandler(Map.of(connector1,
                                                                      handler1,
                                                                      connector2,
                                                                      handler2));

    @Test
    void startsAndStopsAllHandlers() throws Exception {
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
    void routesRequestsToTheConnectorSpecificHandler() throws Exception {
        final ConnectionMetaData connectionMetaData = mock(ConnectionMetaData.class);
        when(connectionMetaData.getConnector()).thenReturn(connector1);

        final Request request = mock(Request.class);
        when(request.getConnectionMetaData()).thenReturn(connectionMetaData);
        final Response response = mock(Response.class);
        final Callback callback = mock(Callback.class);

        handler.handle(request, response, callback);

        verify(handler1).handle(request, response, callback);
    }

    @Test
    void withSessionHandler() throws Exception {
        final ContextHandler handler1 = new ContextHandler();
        final ServletContextHandler handler2 = new ServletContextHandler();
        final SessionHandler childHandler1 = new SessionHandler();
        handler2.setSessionHandler(childHandler1);
        final RoutingHandler handler = new RoutingHandler(Map.of(connector1, handler1, connector2, handler2));
        Server server = new Server();
        server.setHandler(handler);

        server.start();
        handler.start();
        try {
            assertThat(getSessionHandlers(handler)).containsOnly(childHandler1);
        } finally {
            handler.stop();
            server.stop();
        }
    }

    @Test
    void withoutSessionHandler() throws Exception {
        new Server().setHandler(handler);

        handler.start();
        try {
            assertThat(getSessionHandlers(handler)).isEmpty();
        } finally {
            handler.stop();
        }
    }

    private Set<SessionHandler> getSessionHandlers(final RoutingHandler routingHandler) {
        return routingHandler.getServer().getDescendants(ContextHandler.class).stream()
                .map(handler -> handler.getDescendant(SessionHandler.class))
                .filter(Objects::nonNull).collect(Collectors.toSet());
    }

}
