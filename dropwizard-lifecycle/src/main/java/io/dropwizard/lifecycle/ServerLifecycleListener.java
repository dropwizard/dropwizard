package io.dropwizard.lifecycle;

import org.eclipse.jetty.server.Server;

import java.util.EventListener;

public interface ServerLifecycleListener extends EventListener {
    void serverStarted(Server server);
}
