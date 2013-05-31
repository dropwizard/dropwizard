package com.codahale.dropwizard.cli;

import com.codahale.dropwizard.Application;
import com.codahale.dropwizard.Configuration;
import com.codahale.dropwizard.setup.Bootstrap;
import com.codahale.dropwizard.setup.Environment;
import net.sourceforge.argparse4j.inf.Namespace;

import javax.validation.Validation;

/**
 * A command which executes with a configured {@link Environment}.
 *
 * @param <T> the {@link Configuration} subclass which is loaded from the configuration file
 * @see Configuration
 */
public abstract class EnvironmentCommand<T extends Configuration> extends ConfiguredCommand<T> {
    private final Application<T> application;

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

    @Override
    protected final void run(Bootstrap<T> bootstrap, Namespace namespace, T configuration) throws Exception {
        final Environment environment = new Environment(bootstrap.getApplication().getName(),
                                                        bootstrap.getObjectMapper(),
                                                        Validation.buildDefaultValidatorFactory()
                                                                  .getValidator(),
                                                        bootstrap.getMetricRegistry(),
                                                        bootstrap.getClassLoader());
        configuration.getMetricsFactory().configure(environment.lifecycle(),
                                                    bootstrap.getMetricRegistry());
        bootstrap.runWithBundles(configuration, environment);
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
