package com.yammer.dropwizard.cli;

import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import net.sourceforge.argparse4j.inf.Namespace;

/**
 * A command which executes with a configured {@link Environment}.
 *
 * @param <T> the {@link Configuration} subclass which is loaded from the configuration file
 * @see Configuration
 */
public abstract class EnvironmentCommand<T extends Configuration> extends ConfiguredCommand<T> {
    private final Service<T> service;

    /**
     * Creates a new environment command.
     *
     * @param service     the service providing this command
     * @param name        the name of the command, used for command line invocation
     * @param description a description of the command's purpose
     */
    protected EnvironmentCommand(Service<T> service, String name, String description) {
        super(name, description);
        this.service = service;
    }

    @Override
    protected final void run(Bootstrap<T> bootstrap, Namespace namespace, T configuration) throws Exception {
        final Environment environment = new Environment(bootstrap.getName(),
                                                        configuration,
                                                        bootstrap.getObjectMapperFactory().copy());
        bootstrap.runWithBundles(configuration, environment);
        service.run(configuration, environment);
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
