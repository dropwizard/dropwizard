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
        final Bootstrap<T> bootstrap = createBootstrap();
        bootstrap.addCommand(createServerCommand());
        initialize(bootstrap);
        final Cli cli = createCli(bootstrap);
        cli.run(arguments);
    }

    /**
     * Creates the {@link Bootstrap} object.
     * @return A Bootstrap instance
     */
    protected Bootstrap<T> createBootstrap() {
        return new Bootstrap<T>(this);
    }

    /**
     * Creates the {@link Cli} object that will run the service.
     * @param bootstrap The {@link Bootstrap} of the service.
     * @return A Cli instance.
     */
    protected Cli createCli(Bootstrap<T> bootstrap) {
        return new Cli(this.getClass(), bootstrap);
    }

    /**
     * Creates the {@link ServerCommand} for the service.
     * @return A ServerCommand instance.
     */
    protected ServerCommand<T> createServerCommand() {
        return new ServerCommand<T>(this);
    }
}
