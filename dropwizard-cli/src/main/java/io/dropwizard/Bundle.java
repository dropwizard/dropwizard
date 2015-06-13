package io.dropwizard;

import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;

/**
 * A reusable bundle of functionality, used to define blocks of application behavior.
 */
public interface Bundle {
    /**
     * Initializes the application bootstrap.
     *
     * @param bootstrap the application bootstrap
     */
    void initialize(Bootstrap<?> bootstrap);

    /**
     * Initializes the application environment.
     *
     * @param environment the application environment
     */
    void run(Environment environment);
}
