package io.dropwizard;

import io.dropwizard.setup.HttpEnvironment;

/**
 * A reusable bundle of functionality, used to define blocks of application behavior that are
 * conditional on configuration parameters.
 *
 * @param <T>    the required configuration interface
 */
public interface ConfiguredHttpBundle<T extends HttpConfiguration> extends ConfiguredBundle<T> {
    /**
     * Initializes the environment.
     *
     * @param configuration    the configuration object
     * @param environment      the application's {@link HttpEnvironment}
     * @throws Exception if something goes wrong
     */
    void run(T configuration, HttpEnvironment environment) throws Exception;

}
