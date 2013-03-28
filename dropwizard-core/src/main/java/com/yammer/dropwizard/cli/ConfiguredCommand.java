package com.yammer.dropwizard.cli;

import com.yammer.dropwizard.config.*;
import com.yammer.dropwizard.json.ObjectMapperFactory;
import com.yammer.dropwizard.util.Generics;
import com.yammer.dropwizard.validation.Validator;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import java.io.File;
import java.io.FileNotFoundException;
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
        subparser.addArgument("file").nargs("?").help("service configuration file");
    }

    @Override
    @SuppressWarnings("unchecked")
    public final void run(Bootstrap<?> bootstrap, Namespace namespace) throws Exception {
        final T configuration = parseConfiguration(namespace.getString("file"),
                                                   getConfigurationClass(),
                                                   bootstrap.getObjectMapperFactory().copy());
        if (configuration != null) {
            new LoggingFactory(configuration.getLoggingConfiguration(),
                               bootstrap.getName()).configure();
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

    /**
     * Returns the {@link Configuration} of type T at the specified configuration file path. The
     * default behavior looks for the configuration file on the local file system.
     * Subclasses can override this method as a means to provide a way to read the configuration
     * from any source they choose.
     *
     * @param configurationPath the path to the configuration
     * @param configurationFactory the configurationFactory
     * @return the Configuration object
     * @throws IOException throws an exception if there is an error reading the configuration
     * from the configurationPath
     * @throws ConfigurationException throws an exception in the case of an error with the
     * configuration (i.e. validation)
     */
    protected T parseConfiguration(String configurationPath,
                                    ConfigurationFactory<T> configurationFactory) throws IOException, ConfigurationException {
        if (configurationPath != null) {
            final File file = new File(configurationPath);
            if (!file.exists()) {
                throw new FileNotFoundException("Configuration file " + file + " not found");
            }

            return configurationFactory.build(file);
        }

        return configurationFactory.build();
    }

    private T parseConfiguration(String configurationPath,
                                 Class<T> configurationClass,
                                 ObjectMapperFactory objectMapperFactory) throws IOException, ConfigurationException {
        final ConfigurationFactory<T> configurationFactory =
                ConfigurationFactory.forClass(configurationClass, new Validator(), objectMapperFactory);

        return parseConfiguration(configurationPath, configurationFactory);
    }

}
