package com.codahale.dropwizard.cli;

import com.codahale.dropwizard.Configuration;
import com.codahale.dropwizard.configuration.ConfigurationException;
import com.codahale.dropwizard.configuration.ConfigurationFactory;
import com.codahale.dropwizard.configuration.ConfigurationSourceProvider;
import com.codahale.dropwizard.setup.Bootstrap;
import com.codahale.dropwizard.util.Generics;
import com.fasterxml.jackson.databind.ObjectMapper;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import javax.validation.Validation;
import javax.validation.Validator;
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
    protected ConfiguredCommand(String name, String description) {
        super(name, description);
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
    public final void run(Bootstrap<?> bootstrap, Namespace namespace) throws Exception {
        final T configuration = parseConfiguration(bootstrap.getConfigurationProvider(),
                                                   namespace.getString("file"),
                                                   getConfigurationClass(),
                                                   bootstrap.getObjectMapper());
        if (configuration != null) {
            configuration.getLoggingFactory().configure(bootstrap.getMetricRegistry(),
                                                        bootstrap.getApplication().getName());
        }
        run((Bootstrap<T>) bootstrap, namespace, configuration);
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

    private T parseConfiguration(ConfigurationSourceProvider provider,
                                 String path,
                                 Class<T> klass,
                                 ObjectMapper objectMapper) throws IOException, ConfigurationException {
        final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();
        final ConfigurationFactory<T> configurationFactory =
                new ConfigurationFactory<>(klass, validator, objectMapper, "dw");
        if (path != null) {
            return configurationFactory.build(provider, path);
        }
        return configurationFactory.build();
    }
}
