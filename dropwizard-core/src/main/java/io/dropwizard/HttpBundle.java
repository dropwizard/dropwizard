package io.dropwizard;

import io.dropwizard.setup.HttpEnvironment;

/**
 * A reusable bundle of functionality, used to define blocks of application behavior.
 */
public interface HttpBundle extends Bundle {

    /**
     * Initializes the application http environment.
     *
     * @param environment the application environment
     */
    void run(HttpEnvironment environment);
}
