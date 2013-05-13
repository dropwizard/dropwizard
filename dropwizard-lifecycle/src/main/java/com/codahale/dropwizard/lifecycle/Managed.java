package com.codahale.dropwizard.lifecycle;

/**
 * An interface for objects which need to be started and stopped as the application is started or
 * stopped.
 */
public interface Managed {
    /**
     * Starts the object. Called <i>before</i> the application becomes available.
     *
     * @throws Exception if something goes wrong; this will halt the application startup.
     */
    public void start() throws Exception;

    /**
     * Stops the object. Called <i>after</i> the application is no longer accepting requests.
     *
     * @throws Exception if something goes wrong.
     */
    public void stop() throws Exception;
}
