package io.dropwizard.lifecycle;

/**
 * An interface for objects which need to take some action as the application is started or stopped.
 */
public interface Managed {
    /**
     * Starts the object. Called <i>before</i> the application becomes available. The default implementation is a no-op.
     *
     * @throws Exception if something goes wrong; this will halt the application startup.
     */
    default void start() throws Exception {}

    /**
     * Stops the object. Called <i>after</i> the application is no longer accepting requests. The default implementation
     * is a no-op
     *
     * @throws Exception if something goes wrong.
     */
    default void stop() throws Exception {}
}
