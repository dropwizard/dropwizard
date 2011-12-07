package com.yammer.dropwizard;

import com.yammer.dropwizard.config.Environment;

/**
 * A reusable bundle of functionality, used to define blocks of service behavior.
 */
public interface Bundle {
    /**
     * Initializes the environment.
     *
     * @param environment    the service's {@link Environment}
     */
    public void initialize(Environment environment);
}
