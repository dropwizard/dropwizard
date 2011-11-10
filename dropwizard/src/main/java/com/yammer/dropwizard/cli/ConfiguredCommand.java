package com.yammer.dropwizard.cli;

import com.google.common.base.Optional;
import com.yammer.dropwizard.AbstractService;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.ConfigurationException;
import com.yammer.dropwizard.config.ConfigurationFactory;
import com.yammer.dropwizard.config.LoggingFactory;
import com.yammer.dropwizard.util.Validator;
import org.apache.commons.cli.CommandLine;

import java.io.File;
import java.lang.reflect.ParameterizedType;

// TODO: 10/12/11 <coda> -- write tests for ConfiguredCommand
// TODO: 10/12/11 <coda> -- write docs for ConfiguredCommand

public abstract class ConfiguredCommand<T extends Configuration> extends Command {
    protected ConfiguredCommand(String name) {
        super(name, null);
    }

    protected ConfiguredCommand(String name,
                                String description) {
        super(name, description);
    }

    @SuppressWarnings("unchecked")
    protected Class<T> getConfigurationClass() {
        return (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[0];
    }

    protected Optional<String> getConfiguredSyntax() {
        return Optional.absent();
    }

    @Override
    protected final String getSyntax() {
        final StringBuilder syntax = new StringBuilder("<config file>");
        final Optional<String> configured = getConfiguredSyntax();
        if (configured.isPresent() && !configured.get().isEmpty()) {
            syntax.append(' ').append(configured.get());
        }
        return syntax.toString();
    }

    @Override
    @SuppressWarnings("unchecked")
    protected final void run(AbstractService<?> service,
                             CommandLine params) throws Exception {
        LoggingFactory.bootstrap();
        final String[] args = params.getArgs();
        if (args.length == 1) {
            try {
                final ConfigurationFactory<T> factory = new ConfigurationFactory<T>(getConfigurationClass(),
                                                                                    new Validator());
                final T configuration = factory.build(new File(args[0]));
                run((AbstractService<T>) service, configuration, params);
            } catch (ConfigurationException e) {
                UsagePrinter.printCommandHelp(this, Optional.fromNullable(e.getMessage()));
            }
        } else {
            UsagePrinter.printCommandHelp(this);
            System.exit(-1);
        }
    }

    protected abstract void run(AbstractService<T> service,
                                T configuration,
                                CommandLine params) throws  Exception;
}
