package io.dropwizard.setup;

import io.dropwizard.Bundle;
import io.dropwizard.Configuration;
import io.dropwizard.ConfiguredBundle;

/**
 * Shim to allow the use of {@link io.dropwizard.Bundle}s in the same collections as {@link ConfiguredBundle}s.
 *
 * @param <T>
 *         the configuration type of the application.
 */
class ConfiguredBundleAdapter<T extends Configuration> implements ConfiguredBundle<T> {
    private final Bundle bundle;

    /**
     * Wrap a bundle in this adapter, allowing it to act as a ConfiguredBundle (which ignores its configuration).
     *
     * @param bundle
     *         the bundle to wrap.
     */
    public ConfiguredBundleAdapter(Bundle bundle) {
        this.bundle = bundle;
    }

    @Override
    public void initialize(Bootstrap<?> bootstrap) {
        bundle.initialize(bootstrap);
    }

    @Override
    public void run(T configuration, Environment environment) throws Exception {
        bundle.run(environment);
    }
}
