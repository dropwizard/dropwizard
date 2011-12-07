package com.yammer.dropwizard;

import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.bundles.JavaBundle;

/**
 * The default Java service class. Extend this to write your own service.
 *
 * @param <T>    the type of configuration class to use
 * @see Configuration
 */
public abstract class Service<T extends Configuration> extends AbstractService<T> {
    protected Service(String name) {
        super(name);
        addBundle(new JavaBundle());
    }

    @Override
    protected final void subclassServiceInsteadOfThis() {

    }
}
