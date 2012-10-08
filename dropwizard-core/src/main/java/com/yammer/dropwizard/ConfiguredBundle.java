package com.yammer.dropwizard;

import com.yammer.dropwizard.config.Environment;

/**
 * A reusable bundle of functionality, used to define blocks of service behavior that are
 * conditional on configuration parameters.
 *
 * @param <T>    the required configuration interface
 */
public interface ConfiguredBundle<T> {
    /**
     * Initializes the environment.
     *
     * @param configuration    the configuration object
     * @param environment      the service's {@link Environment}
     */
    public void run(T configuration, Environment environment);
}
