package io.dropwizard;

import io.dropwizard.setup.Environment;
import io.dropwizard.setup.HttpEnvironment;

public abstract class AbstractConfiguredHttpBundle<T extends HttpConfiguration> implements ConfiguredHttpBundle<T> {

    @Override
    public final void run(T configuration, Environment environment) throws Exception {
        if (environment instanceof HttpEnvironment) {
            run(configuration, (HttpEnvironment) environment);
        } else {
            throw new IllegalStateException("AbstractHttpBundle requires HttpEnvironment");
        }
    }

}
