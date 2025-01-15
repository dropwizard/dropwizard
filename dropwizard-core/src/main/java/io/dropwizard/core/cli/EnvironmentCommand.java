package io.dropwizard.core.cli;

import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.core.setup.Environment;
import net.sourceforge.argparse4j.inf.Namespace;
import org.jspecify.annotations.Nullable;

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
                environment.lifecycle(),
                environment.servlets(),
                environment.jersey(),
                environment.health(),
                environment.getObjectMapper(),
                application.getName()));

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
