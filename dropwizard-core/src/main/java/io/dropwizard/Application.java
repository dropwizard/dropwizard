package io.dropwizard;

import ch.qos.logback.classic.Level;
import io.dropwizard.cli.CheckCommand;
import io.dropwizard.cli.Cli;
import io.dropwizard.cli.ServerCommand;
import io.dropwizard.logging.BootstrapLogging;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Generics;
import io.dropwizard.util.JarLocation;

/**
 * The base class for Dropwizard applications.
 *
 * Because the default constructor will be inherited by all
 * subclasses, {BootstrapLogging.bootstrap()} will always be
 * invoked. The log level used during the bootstrap process can be
 * configured by {Application} subclasses by overriding
 * {#bootstrapLogLevel}.
 *
 * @param <T> the type of configuration class for this application
 */
public abstract class Application<T extends Configuration> {
    protected Application() {
        bootstrapLogging();
    }

    /**
     * The log level at which to bootstrap logging on application startup.
     */
    protected Level bootstrapLogLevel() {
        return Level.WARN;
    }

    protected void bootstrapLogging() {
        // make sure spinning up Hibernate Validator doesn't yell at us
        BootstrapLogging.bootstrap(bootstrapLogLevel());
    }

    /**
     * Returns the {@link Class} of the configuration class type parameter.
     *
     * @return the configuration class
     * @see Generics#getTypeParameter(Class, Class)
     */
    public Class<T> getConfigurationClass() {
        return Generics.getTypeParameter(getClass(), Configuration.class);
    }

    /**
     * Returns the name of the application.
     *
     * @return the application's name
     */
    public String getName() {
        return getClass().getSimpleName();
    }

    /**
     * Initializes the application bootstrap.
     *
     * @param bootstrap the application bootstrap
     */
    public void initialize(Bootstrap<T> bootstrap) {
    }

    /**
     * When the application runs, this is called after the {@link Bundle}s are run. Override it to add
     * providers, resources, etc. for your application.
     *
     * @param configuration the parsed {@link Configuration} object
     * @param environment   the application's {@link Environment}
     * @throws Exception if something goes wrong
     */
    public abstract void run(T configuration, Environment environment) throws Exception;

    /**
     * Parses command-line arguments and runs the application. Call this method from a {@code public
     * static void main} entry point in your application.
     *
     * @param arguments the command-line arguments
     * @throws Exception if something goes wrong
     */
    public void run(String... arguments) throws Exception {
        final Bootstrap<T> bootstrap = new Bootstrap<>(this);
        addDefaultCommands(bootstrap);
        initialize(bootstrap);
        // Should be called after initialize to give an opportunity to set a custom metric registry
        bootstrap.registerMetrics();

        final Cli cli = new Cli(new JarLocation(getClass()), bootstrap, System.out, System.err);
        if (!cli.run(arguments)) {
            // only exit if there's an error running the command
            onFatalError();
        }
    }

    /**
     * Called by {@link #run(String...)} to add the standard "server" and "check" commands
     *
     * @param bootstrap the bootstrap instance
     */
    protected void addDefaultCommands(Bootstrap<T> bootstrap) {
        bootstrap.addCommand(new ServerCommand<>(this));
        bootstrap.addCommand(new CheckCommand<>(this));
    }

    /**
     * Called by {@link #run(String...)} to indicate there was a fatal error running the requested command.
     *
     * The default implementation calls {@link System#exit(int)} with a non-zero status code to terminate the
     * application.
     */
    protected void onFatalError() {
        System.exit(1);
    }
}
