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
import java.lang.reflect.Type;

/**
 * A command whose first parameter is the location of a YAML configuration file. That file is parsed
 *
 * @param <T> the {@link Configuration} subclass which is loaded from the configuration file
 * @see Configuration
 */
public abstract class ConfiguredCommand<T extends Configuration> extends Command {
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
        Type t = getClass();
        while (t instanceof Class<?>) {
            t = ((Class<?>) t).getGenericSuperclass();
        }
        /* This is not guaranteed to work for all cases with convoluted piping
         * of type parameters: but it can at least resolve straight-forward
         * extension with single type parameter (as per [Issue-89]).
         * And when it fails to do that, will indicate with specific exception.
         */
        if (t instanceof ParameterizedType) {
            // should typically have one of type parameters (first one) that matches:
            for (Type param : ((ParameterizedType) t).getActualTypeArguments()) {
                if (param instanceof Class<?>) {
                    final Class<?> cls = (Class<?>) param;
                    if (Configuration.class.isAssignableFrom(cls)) {
                        return (Class<T>) cls;
                    }
                }
            }
        }
        throw new IllegalStateException("Can not figure out Configuration type parameterization for "+getClass().getName());
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
        final StringBuilder syntax = new StringBuilder("[config file]");
        final String configured = getConfiguredSyntax();
        if ((configured != null) && !configured.isEmpty()) {
            syntax.append(' ').append(configured);
        }
        return syntax.toString();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected final void run(AbstractService<?> service, CommandLine params) throws Exception {
        final String[] args = params.getArgs();
        final Class<T> configurationClass = getConfigurationClass();
        T configuration = null;
        final ConfigurationFactory<T> configurationFactory =
                ConfigurationFactory.forClass(configurationClass, new Validator(), service.getJacksonModules());
        try {
            if (args.length >= 1) {
                params.getArgList().remove(0);
                configuration = configurationFactory.build(new File(args[0]));
            } else {
                configuration = configurationFactory.build();
            }
        } catch (ConfigurationException e) {
            printHelp(e.getMessage(), service.getClass());
            System.exit(1);
        }

        if (configuration != null) {
            new LoggingFactory(configuration.getLoggingConfiguration(), service.getName()).configure();
            run((AbstractService<T>)service, configuration, params);
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
