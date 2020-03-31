package io.dropwizard;

import io.dropwizard.setup.Environment;

/**
 * A reusable bundle of functionality, used to define blocks of application behavior.
 *
 * @deprecated Use {@link ConfiguredBundle}
 */
@Deprecated
public interface Bundle extends ConfiguredBundle<Configuration> {
    @Override
    default void run(Configuration configuration, Environment environment) throws Exception {
        run(environment);
    }

    /**
     * Initializes the application environment.
     *
     * @param environment the application environment
     * @deprecated Use {@link ConfiguredBundle#run(Configuration, Environment)}
     */
    @Deprecated
    default void run(Environment environment) {
        // Do nothing
    }
}
