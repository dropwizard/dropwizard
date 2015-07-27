package io.dropwizard;

import io.dropwizard.setup.Environment;
import io.dropwizard.setup.HttpEnvironment;

public abstract class AbstractHttpBundle implements HttpBundle {

    @Override
    public final void run(Environment environment) {
        if (environment instanceof HttpEnvironment) {
            run((HttpEnvironment) environment);
        } else {
            throw new IllegalStateException("AbstractHttpBundle requires HttpEnvironment");
        }
    }

}
