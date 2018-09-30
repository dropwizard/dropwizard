package io.dropwizard;

import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * A reusable bundle of functionality, used to define blocks of application behavior
 *
 * @param <T>    the required configuration interface, use {@link Configuration} if specific config not required
 */
public interface Bundle<T> {

    /**
     * Initializes the application bootstrap.
     *
     * @param bootstrap the application bootstrap
     */
    void initialize(Bootstrap<?> bootstrap);

    /**
     * Initializes the environment.
     *
     * @param configuration    the configuration object
     * @param environment      the application's {@link Environment}
     * @throws Exception if something goes wrong
     */
    void run(T configuration, Environment environment) throws Exception;
}
