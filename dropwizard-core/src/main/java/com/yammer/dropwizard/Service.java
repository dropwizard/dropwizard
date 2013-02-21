package com.yammer.dropwizard;

import com.yammer.dropwizard.cli.Cli;
import com.yammer.dropwizard.cli.ServerCommand;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.config.LoggingFactory;
import com.yammer.dropwizard.util.Generics;

/**
 * The base class for Dropwizard services.
 *
 * @param <T> the type of configuration class for this service
 */
public abstract class Service<T extends Configuration> {
    static {
        // make sure spinning up Hibernate Validator doesn't yell at us
        LoggingFactory.bootstrap();
    }

    /**
     * Returns the {@link Class} of the configuration class type parameter.
     *
     * @return the configuration class
     * @see Generics#getTypeParameter(Class, Class)
     */
    public final Class<T> getConfigurationClass() {
        return Generics.getTypeParameter(getClass(), Configuration.class);
    }

    /**
     * Initializes the service bootstrap.
     *
     * @param bootstrap the service bootstrap
     */
    public abstract void initialize(Bootstrap<T> bootstrap);

    /**
     * When the service runs, this is called after the {@link Bundle}s are run. Override it to add
     * providers, resources, etc. for your service.
     *
     * @param configuration the parsed {@link Configuration} object
     * @param environment   the service's {@link Environment}
     * @throws Exception if something goes wrong
     */
    public abstract void run(T configuration, Environment environment) throws Exception;

    /**
     * Parses command-line arguments and runs the service. Call this method from a {@code public
     * static void main} entry point in your application.
     *
     * @param arguments the command-line arguments
     * @throws Exception if something goes wrong
     */
    public final void run(String[] arguments) throws Exception {
        final Bootstrap<T> bootstrap = new Bootstrap<T>(this);
        bootstrap.addCommand(new ServerCommand<T>(this));
        initialize(bootstrap);
        final Cli cli = new Cli(this.getClass(), bootstrap);
        cli.run(arguments);
    }
}
