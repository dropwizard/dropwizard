package com.codahale.dropwizard.lifecycle;

/**
 * An interface for objects which need to be started and stopped as the service is started or
 * stopped.
 */
public interface Managed {
    /**
     * Starts the object. Called <i>before</i> the service becomes available.
     *
     * @throws Exception if something goes wrong; this will halt the service startup.
     */
    public void start() throws Exception;

    /**
     * Stops the object. Called <i>after</i> the service is no longer accepting requests.
     *
     * @throws Exception if something goes wrong.
     */
    public void stop() throws Exception;
}
