package io.dropwizard.embedded;

import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Generics;

/**
 * Runs an embeddable HTTP service powered by Dropwizard
 * This class is the main interface for embedding and controlling the service,
 * and replaces io.dropwizard.Application in a fairly straightforward manner.
 * The Dropwizard CLI has been replaced with start() and stop() methods, though
 * configuration and other elements remain the same.
 *
 * @param <T> the type of {@link io.dropwizard.Configuration} class for this application
 */
public abstract class Service<T extends Configuration> {
    private EmbeddedBootstrap<T> bootstrap;
    private EmbeddedServer<T> server;
    private String configurationFile;

    public Service(String configFile) {
        this.configurationFile = configFile;
    }

    /**
     * Returns the {@link Class} of the configuration class type parameter.
     *
     * @return the configuration class
     * @see io.dropwizard.util.Generics#getTypeParameter(Class, Class)
     */
    public final Class<T> getConfigurationClass() {
        return Generics.getTypeParameter(getClass(), Configuration.class);
    }

    /**
     * Returns the name of the service.
     *
     * @return the service's name
     */
    public String getName() {
        return getClass().getSimpleName();
    }

    /**
     * Initializes the application bootstrap.
     *
     * @param bootstrap the application bootstrap
     */
    public abstract void initialize(EmbeddedBootstrap<T> bootstrap);

    /**
     * When the service starts, this is called after the {@link io.dropwizard.Bundle}s are run. Override it to add
     * providers, resources, etc. for your application.
     *
     * @param configuration the parsed {@link Configuration} object
     * @param environment   the application's {@link Environment}
     * @throws Exception if something goes wrong
     */
    protected abstract void start(T configuration, Environment environment) throws Exception;

    /**
     * Called when the service is shutting down.  It is not necessary to override this function.
     *
     * @param environment   the service's {@link io.dropwizard.setup.Environment}
     * @throws Exception if something goes wrong
     */
    protected void stop(Environment environment) throws Exception {}

    /**
     * Starts the Dropwizard service.  This is the main entry point.
     *
     * @throws Exception if something goes wrong
     */
    public final void start() throws Exception {
        this.bootstrap = new EmbeddedBootstrap<>(this);
        initialize(bootstrap);
        this.server = new EmbeddedServer<>(this);
        this.server.start(bootstrap, configurationFile);
    }

    /**
     * Stops the Dropwizard service.
     *
     * @throws Exception if something goes wrong
     */
    public final void stop() throws Exception {
        if(this.server != null)
            this.server.stop();
    }
}
