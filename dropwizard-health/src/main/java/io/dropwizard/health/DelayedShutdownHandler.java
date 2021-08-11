package io.dropwizard.health;

import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.thread.ShutdownThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Handler that sets {@code healthy} flag to {@code false} and delays shutdown to allow for a load balancer to
 * determine the instance should no longer receive requests.
 */
public class DelayedShutdownHandler extends AbstractLifeCycle {
    private static final Logger LOGGER = LoggerFactory.getLogger(DelayedShutdownHandler.class);

    private final ShutdownNotifier shutdownNotifier;

    public DelayedShutdownHandler(final ShutdownNotifier shutdownNotifier) {
        this.shutdownNotifier = shutdownNotifier;
    }

    public void register() {
        try {
            start(); // lifecycle must be started in order for stop() to be called

            // register the shutdown handler as first (index 0) so that it executes before Jetty's shutdown behavior
            ShutdownThread.register(0, this);
        } catch (Exception e) {
            LOGGER.error("failed setting up delayed shutdown handler", e);
            throw new IllegalStateException("failed setting up delayed shutdown handler", e);
        }
    }

    @Override
    protected void doStop() throws Exception {
        shutdownNotifier.notifyShutdownStarted();
    }
}
