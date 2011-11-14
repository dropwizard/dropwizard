package com.yammer.dropwizard;

import com.yammer.dropwizard.config.Environment;

/**
 * A reusable module, used to define blocks of service behavior.
 */
public interface Module {
    /**
     * Initializes the environment.
     *
     * @param environment    the service's {@link Environment}
     */
    public void initialize(Environment environment);
}
