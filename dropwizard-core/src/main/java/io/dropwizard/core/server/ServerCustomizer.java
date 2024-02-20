package io.dropwizard.core.server;

import org.eclipse.jetty.server.Handler;

/**
 * A customizer for {@link org.eclipse.jetty.server.Server} objects to be called in a {@link ServerFactory}.
 */
public interface ServerCustomizer {

    /**
     * Allows customizing the server's handler chain. The first handler of the current handler chain built by a
     * {@link ServerFactory} is provided to allow adding a handler at an arbitrary point of the handler chain.
     * The execution order of {@link ServerCustomizer ServerCustomizers} is not guaranteed.
     *
     * @param first the currently first handler in the {@link org.eclipse.jetty.server.Server Server's} handler chain
     * @return the new first handler of the handler chain
     */
    Handler customizeHandlerChain(Handler first);
}
