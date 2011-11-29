package com.yammer.dropwizard.cli;

import com.yammer.dropwizard.AbstractService;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.ConfigurationException;
import com.yammer.dropwizard.config.ConfigurationFactory;
import com.yammer.dropwizard.config.LoggingFactory;
import com.yammer.dropwizard.validation.Validator;
import org.apache.commons.cli.CommandLine;

import java.io.File;
import java.lang.reflect.ParameterizedType;

// TODO: 10/12/11 <coda> -- write tests for ConfiguredCommand

/**
 * A command whose first parameter is the location of a YAML configuration file. That file is parsed
 *
 * @param <T> the {@link Configuration} subclass which is loaded from the configuration file
 * @see Configuration
 */
public abstract class ConfiguredCommand<T extends Configuration> extends Command {
    /**
     * Creates a new {@link ConfiguredCommand} with the given name.
     *
     * @param name    the command's name
     */
    protected ConfiguredCommand(String name) {
        super(name, null);
    }

    /**
     * Creates a new {@link ConfiguredCommand} with the given name and configuration.
     *
     * @param name           the command's name
     * @param description    a description of the command
     */
    protected ConfiguredCommand(String name,
                                String description) {
        super(name, description);
    }

    @SuppressWarnings("unchecked")
    protected Class<T> getConfigurationClass() {
        return (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    /**
     * Returns the usage syntax, minus the configuration file param.
     *
     * @return the command's usage syntax
     */
    protected String getConfiguredSyntax() {
        return null;
    }

    @Override
    protected final String getSyntax() {
        final StringBuilder syntax = new StringBuilder("<config file>");
        final String configured = getConfiguredSyntax();
        if ((configured != null) && !configured.isEmpty()) {
            syntax.append(' ').append(configured);
        }
        return syntax.toString();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected final void run(AbstractService<?> service,
                             CommandLine params) throws Exception {
        final ConfigurationFactory<T> factory = new ConfigurationFactory<T>(getConfigurationClass(),
                                                                            new Validator());
        final String[] args = params.getArgs();
        if (args.length >= 1) {
            params.getArgList().remove(0);
            try {
                final T configuration = factory.build(new File(args[0]));
                new LoggingFactory(configuration.getLoggingConfiguration()).configure();
                run((AbstractService<T>) service, configuration, params);
            } catch (ConfigurationException e) {
                printHelp(e.getMessage());
            }
        } else {
            printHelp();
            System.exit(-1);
        }
    }

    /**
     * Runs the command with the given {@link AbstractService} and {@link Configuration}.
     *
     * @param service          the service to which the command belongs
     * @param configuration    the configuration object
     * @param params           any additional command-line parameters
     * @throws Exception if something goes wrong
     */
    protected abstract void run(AbstractService<T> service,
                                T configuration,
                                CommandLine params) throws  Exception;
}
