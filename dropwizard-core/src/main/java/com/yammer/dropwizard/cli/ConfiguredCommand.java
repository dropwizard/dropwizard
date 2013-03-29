package com.yammer.dropwizard.cli;

import com.yammer.dropwizard.config.*;
import com.yammer.dropwizard.json.ObjectMapperFactory;
import com.yammer.dropwizard.util.Generics;
import com.yammer.dropwizard.validation.Validator;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;

import java.io.*;

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
     * Returns the {@link InputStream) for the configuration associated with this command. By default
     * the method will look for the configuration on the local file system. Subclasses should override
     * this method if it wants to read the configuration from a location other than the local file system
     * (i.e. a remote URL, create it programatically, etc).
     *
     * @param configurationPath the path to the configuration
     * @return the {@link InputStream} that represents the configuration
     */
    protected InputStream getConfigurationInputStream(String configurationPath) throws IOException {
        InputStream configurationInputStream = null;

        if (configurationPath != null) {
            final File file = new File(configurationPath);
            if (!file.exists()) {
                throw new FileNotFoundException("Configuration file " + file + " not found");
            }

            configurationInputStream = new FileInputStream(file);
        }

        return configurationInputStream;
    }

    private T parseConfiguration(String configurationPath,
                                 Class<T> configurationClass,
                                 ObjectMapperFactory objectMapperFactory) throws IOException, ConfigurationException {
        final ConfigurationFactory<T> configurationFactory =
                ConfigurationFactory.forClass(configurationClass, new Validator(), objectMapperFactory);

        InputStream configurationInputStream = getConfigurationInputStream(configurationPath);
        if(configurationInputStream != null) {
            return configurationFactory.build(configurationPath, configurationInputStream);
        }

        return configurationFactory.build();
    }

}
