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

    @SuppressWarnings("unchecked")
    protected Class<T> getConfigurationClass() {
        return (Class<T>) Generics.getTypeParameter(getClass());
    }

    @Override
    public void configure(Subparser subparser) {
        subparser.addArgument("file").nargs("?");
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run(Bootstrap<?> bootstrap, Namespace namespace) throws Exception {
        final T configuration = parseConfiguration(namespace.getString("file"),
                                                   getConfigurationClass(),
                                                   bootstrap.getObjectMapperFactory().clone());
        if (configuration != null) {
            new LoggingFactory(configuration.getLoggingConfiguration(),
                               bootstrap.getName()).configure();
        }
        run((Bootstrap<T>) bootstrap, namespace, configuration);
    }

    private T parseConfiguration(String filename,
                                 Class<T> configurationClass,
                                 ObjectMapperFactory objectMapperFactory) throws IOException, ConfigurationException {
        final ConfigurationFactory<T> configurationFactory =
                ConfigurationFactory.forClass(configurationClass, new Validator(), objectMapperFactory);
        if (filename != null) {
            final File file = new File(filename);
            if (!file.exists()) {
                throw new FileNotFoundException("File " + file + " not found");
            }
            return configurationFactory.build(file);
        }

        return configurationFactory.build();
    }

    /**
     * Runs the command with the given {@link com.yammer.dropwizard.config.Bootstrap} and {@link Configuration}.
     *
     * @param bootstrap      the bootstrap bootstrap
     * @param configuration    the configuration object
     * @throws Exception if something goes wrong
     */
    protected abstract void run(Bootstrap<T> bootstrap,
                                Namespace namespace,
                                T configuration) throws Exception;
}
