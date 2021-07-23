package io.dropwizard.cli;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import javax.annotation.Nullable;
import net.sourceforge.argparse4j.inf.Namespace;

/**
 * A command which executes with a configured {@link Environment}.
 *
 * @param <T> the {@link Configuration} subclass which is loaded from the configuration file
 * @see Configuration
 */
public abstract class EnvironmentCommand<T extends Configuration> extends ConfiguredCommand<T> {
    private final Application<T> application;
    @Nullable
    private Environment environment;

    /**
     * Creates a new environment command.
     *
     * @param application     the application providing this command
     * @param name        the name of the command, used for command line invocation
     * @param description a description of the command's purpose
     */
    protected EnvironmentCommand(Application<T> application, String name, String description) {
        super(name, description);
        this.application = application;
    }

    /**
     * Returns the constructed environment or {@code null} if it hasn't been constructed yet.
     *
     * @return Returns the constructed environment or {@code null} if it hasn't been constructed yet
     * @since 2.0.19
     */
    @Nullable
    public Environment getEnvironment() {
        return environment;
    }

    @SuppressWarnings("NullAway")
    @Override
    protected void run(Bootstrap<T> bootstrap, Namespace namespace, T configuration) throws Exception {
        this.environment = new Environment(bootstrap.getApplication().getName(),
                                           bootstrap.getObjectMapper(),
                                           bootstrap.getValidatorFactory(),
                                           bootstrap.getMetricRegistry(),
                                           bootstrap.getClassLoader(),
                                           bootstrap.getHealthCheckRegistry(),
                                           configuration);
        configuration.getMetricsFactory().configure(environment.lifecycle(),
                                                    bootstrap.getMetricRegistry());
        configuration.getServerFactory().configure(environment);
        configuration.getHealthFactory().ifPresent(health -> health.configure(
                bootstrap.getMetricRegistry(),
                environment.lifecycle(),
                bootstrap.getHealthCheckRegistry(),
                environment.servlets()));

        bootstrap.run(configuration, environment);
        application.run(configuration, environment);
        run(environment, namespace, configuration);
    }

    /**
     * Runs the command with the given {@link Environment} and {@link Configuration}.
     *
     * @param environment   the configured environment
     * @param namespace     the parsed command line namespace
     * @param configuration the configuration object
     * @throws Exception if something goes wrong
     */
    protected abstract void run(Environment environment, Namespace namespace, T configuration) throws Exception;
}
