package com.yammer.dropwizard.cli;

import com.beust.jcommander.Parameter;
import com.yammer.dropwizard.config.*;
import com.yammer.dropwizard.json.ObjectMapperFactory;
import com.yammer.dropwizard.util.Generics;
import com.yammer.dropwizard.validation.Validator;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * A command whose first parameter is the location of a YAML configuration file. That file is parsed
 * into an instance of a {@link Configuration} subclass, which is then validated. If the
 * configuration is valid, the command is run.
 *
 * @param <T> the {@link Configuration} subclass which is loaded from the configuration file
 * @see Configuration
 */
public abstract class ConfiguredCommand<T extends Configuration> implements Command {
    @Parameter(description = "<configuration file>")
    @SuppressWarnings("FieldMayBeFinal")
    private List<String> arguments = new ArrayList<String>(1);

    @SuppressWarnings("unchecked")
    protected Class<T> getConfigurationClass() {
        return (Class<T>) Generics.getTypeParameter(getClass());
    }

    @Override
    @SuppressWarnings("unchecked")
    public void run(Bootstrap<?> bootstrap) throws Exception {
        final T configuration = parseConfiguration(getConfigurationClass(),
                                                   bootstrap.getObjectMapperFactory().clone());
        if (configuration != null) {
            run((Bootstrap<T>) bootstrap, configuration);
        }
    }

    private T parseConfiguration(Class<T> configurationClass,
                                 ObjectMapperFactory objectMapperFactory) throws IOException, ConfigurationException {
        final ConfigurationFactory<T> configurationFactory =
                ConfigurationFactory.forClass(configurationClass, new Validator(), objectMapperFactory);
        if (arguments.size() >= 1) {
            final String file = arguments.remove(0);
            return configurationFactory.build(new File(file));
        }

        return configurationFactory.build();
    }

    /**
     * Runs the command with the given {@link com.yammer.dropwizard.Service} and {@link Configuration}.
     *
     * @param bootstrap      the bootstrap bootstrap
     * @param configuration    the configuration object
     * @throws Exception if something goes wrong
     */
    protected abstract void run(Bootstrap<T> bootstrap,
                                T configuration) throws  Exception;

    protected final List<String> getArguments() {
        return arguments;
    }
}
