package com.yammer.dropwizard;

import com.yammer.dropwizard.config.Bootstrap;
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
     * @throws Exception if something goes wrong
     */
    void run(T configuration, Environment environment) throws Exception;

    /**
     * Initializes the service bootstrap.
     *
     * @param bootstrap the service bootstrap
     */
    void initialize(Bootstrap<?> bootstrap);
}
