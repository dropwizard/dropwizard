package com.yammer.dropwizard;

import com.yammer.dropwizard.bundles.JavaBundle;
import com.yammer.dropwizard.config.Configuration;

/**
 * The default Java service class. Extend this to write your own service.
 *
 * @param <T>    the type of configuration class to use
 * @see Configuration
 */
public abstract class Service<T extends Configuration> extends AbstractService<T> {

    protected Service(String name) {
        super(name);
        addBundle(new JavaBundle(this));
        checkForScalaExtensions();
    }

    protected Service() {
        this(null);
    }

    @Override
    protected final void subclassServiceInsteadOfThis() {

    }

    private void checkForScalaExtensions() {
        try {
            final Class<?> scalaObject = Class.forName("scala.ScalaObject");
            final Class<?> klass = getClass();
            if (scalaObject.isAssignableFrom(klass)) {
                throw new IllegalStateException(klass.getCanonicalName() + " is a Scala class. " +
                                                        "It should extend ScalaService, not Service.");
            }
        } catch (ClassNotFoundException ignored) {
            // definitely not a Scala project
        }
    }
}
