package io.dropwizard.lifecycle;

import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import java.util.EventListener;

public interface ServerLifecycleListener extends EventListener {
    void serverStarted(Server server);

    default int getLocalPort(Server server) {
        return ((ServerConnector) server.getConnectors()[0]).getLocalPort();
    }

    default int getAdminPort(Server server) {
        final Connector[] connectors = server.getConnectors();
        return ((ServerConnector) connectors[connectors.length -1]).getLocalPort();
    }
}
