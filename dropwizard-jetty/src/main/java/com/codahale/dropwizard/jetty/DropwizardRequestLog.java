package com.codahale.dropwizard.jetty;


import ch.qos.logback.access.jetty.RequestLogImpl;

/**
 * A base layout for Dropwizard.
 * <ul>
 *     <li>Disables pattern headers.</li>
 *     <li>Prefixes logged exceptions with {@code !}.</li>
 *     <li>Sets the pattern to the given timezine.</li>
 * </ul>
 */
public class DropwizardRequestLog extends RequestLogImpl {

    boolean started = false;

    @Override
    public void start() {
        started = true;
    }

    @Override
    public boolean isStarted() {
        return started;
    }

    @Override
    public boolean isRunning() {
        return started;
    }

    @Override
    public boolean isStopped() {
        return !started;
    }

    @Override
    public void stop() {
        detachAndStopAllAppenders();
        started = false;
    }

}

