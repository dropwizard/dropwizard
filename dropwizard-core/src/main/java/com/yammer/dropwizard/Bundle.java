package com.yammer.dropwizard;

import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Environment;

/**
 * A reusable bundle of functionality, used to define blocks of service behavior.
 */
@SuppressWarnings("UnusedParameters")
public abstract class Bundle {
    /**
     * Initializes the service bootstrap.
     *
     * @param bootstrap the service bootstrap
     */
    public void initialize(Bootstrap<?> bootstrap) {
        // no default impl
    }

    /**
     * Initializes the service environment.
     *
     * @param environment the service environment
     */
    public void run(Environment environment) {
        // no default impl
    }
}
