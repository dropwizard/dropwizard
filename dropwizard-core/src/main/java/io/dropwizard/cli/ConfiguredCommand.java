package io.dropwizard.cli;

import io.dropwizard.Configuration;
import io.dropwizard.configuration.ConfigurationException;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.configuration.ConfigurationFactoryFactory;
import io.dropwizard.configuration.ConfigurationSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.util.Generics;

import java.io.IOException;

import javax.validation.Validator;

import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * A command whose first parameter is the location of a YAML configuration file. That file is parsed
 * into an instance of a {@link Configuration} subclass, which is then validated. If the
 * configuration is valid, the command is run.
 *
 * @param <T> the {@link Configuration} subclass which is loaded from the configuration file
 * @see Configuration
 */
public abstract class ConfiguredCommand<T extends Configuration> extends Command {
    private boolean asynchronous;

    private T configuration;

    protected ConfiguredCommand(String name, String description) {
        super(name, description);
        this.asynchronous = false;
    }

    /**
     * Returns the {@link Class} of the configuration type.
     *
     * @return the {@link Class} of the configuration type
     */
    protected Class<T> getConfigurationClass() {
        return Generics.getTypeParameter(getClass(), Configuration.class);
    }

    /**
     * Configure the command's {@link Subparser}. <p><strong> N.B.: if you override this method, you
     * <em>must</em> call {@code super.override(subparser)} in order to preserve the configuration
     * file parameter in the subparser. </strong></p>
     *
     * @param subparser the {@link Subparser} specific to the command
     */
    @Override
    public void configure(Subparser subparser) {
        subparser.addArgument("file").nargs("?").help("application configuration file");
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run(Bootstrap<?> wildcardBootstrap, Namespace namespace) throws Exception {
        final Bootstrap<T> bootstrap = (Bootstrap<T>) wildcardBootstrap;
        configuration = parseConfiguration(bootstrap.getConfigurationFactoryFactory(),
                                           bootstrap.getConfigurationSourceProvider(),
                                           bootstrap.getValidatorFactory().getValidator(),
                                           namespace.getString("file"),
                                           getConfigurationClass(),
                                           bootstrap.getObjectMapper());

        try {
            if (configuration != null) {
                configuration.getLoggingFactory().configure(bootstrap.getMetricRegistry(),
                                                            bootstrap.getApplication().getName());
            }

            run(bootstrap, namespace, configuration);
        } finally {
            if (!asynchronous) {
                cleanup();
            }
        }
    }

    protected void cleanupAsynchronously() {
        this.asynchronous = true;
    }

    protected void cleanup() {
        if (configuration != null) {
            configuration.getLoggingFactory().stop();
        }
    }

    /**
     * Runs the command with the given {@link Bootstrap} and {@link Configuration}.
     *
     * @param bootstrap     the bootstrap bootstrap
     * @param namespace     the parsed command line namespace
     * @param configuration the configuration object
     * @throws Exception if something goes wrong
     */
    protected abstract void run(Bootstrap<T> bootstrap,
                                Namespace namespace,
                                T configuration) throws Exception;

    private T parseConfiguration(ConfigurationFactoryFactory<T> configurationFactoryFactory,
                                 ConfigurationSourceProvider provider,
                                 Validator validator,
                                 String path,
                                 Class<T> klass,
                                 ObjectMapper objectMapper) throws IOException, ConfigurationException {
        final ConfigurationFactory<T> configurationFactory = configurationFactoryFactory
                .create(klass, validator, objectMapper, "dw");
        if (path != null) {
            return configurationFactory.build(provider, path);
        }
        return configurationFactory.build();
    }
}
