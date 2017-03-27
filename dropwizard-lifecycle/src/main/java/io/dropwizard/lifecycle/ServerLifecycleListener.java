package io.dropwizard.lifecycle;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import java.util.EventListener;

public interface ServerLifecycleListener extends EventListener {

    void serverStarted(Server server);

    /**
     * Return the local port of the first {@link ServerConnector} in the
     * provided {@link Server} instance.
     *
     * @param server Server instance to use
     * @return First local port of the server instance
     */
    default int getLocalPort(Server server) {
        return ((ServerConnector) server.getConnectors()[0]).getLocalPort();
    }

    /**
     * Return the local port of the last {@link ServerConnector} in the
     * provided {@link Server} instance. This may be the same value as returned
     * by {@link #getLocalPort(Server)} if using the "simple" server configuration.
     *
     * @param server Server instance to use
     * @return Last local port or the server instance
     */
    default int getAdminPort(Server server) {
        final Connector[] connectors = server.getConnectors();
        return ((ServerConnector) connectors[connectors.length - 1]).getLocalPort();
    }
}
