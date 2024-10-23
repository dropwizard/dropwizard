package io.dropwizard.core.cli;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.configuration.ConfigurationException;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.configuration.ConfigurationFactoryFactory;
import io.dropwizard.configuration.ConfigurationSourceProvider;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Bootstrap;
import io.dropwizard.util.Generics;
import jakarta.validation.Validator;
import net.sourceforge.argparse4j.inf.Argument;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.io.IOException;

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

    @Nullable
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
     * Returns the parsed configuration or {@code null} if it hasn't been parsed yet.
     *
     * @return Returns the parsed configuration or {@code null} if it hasn't been parsed yet
     * @since 2.0.19
     */
    @Nullable
    public T getConfiguration() {
        return configuration;
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
        addFileArgument(subparser);
    }

    /**
     * Adds the configuration file argument for the configured command.
     * @param subparser The subparser to register the argument on
     * @return the register argument
     */
    protected Argument addFileArgument(Subparser subparser) {
        return subparser.addArgument("file")
                        .nargs("?")
                        .help("application configuration file");
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
            } else if (configuration != null) {
                configuration.getLoggingFactory().stop();
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
     * @param bootstrap     the bootstrap
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
