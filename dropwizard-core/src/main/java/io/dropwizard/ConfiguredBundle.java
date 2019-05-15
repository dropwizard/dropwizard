package io.dropwizard;

import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * A reusable bundle of functionality, used to define blocks of application behavior that are
 * conditional on configuration parameters.
 *
 * @param <T>    the required configuration interface
 */
public interface ConfiguredBundle<T> {
    /**
     * Initializes the environment.
     *
     * @param configuration    the configuration object
     * @param environment      the application's {@link Environment}
     * @throws Exception if something goes wrong
     */
    default void run(T configuration, Environment environment) throws Exception {
        // Do nothing
    }

    /**
     * Initializes the application bootstrap.
     *
     * @param bootstrap the application bootstrap
     */
    default void initialize(Bootstrap<?> bootstrap) {
        // Do nothing
    }
}
