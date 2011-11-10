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

// TODO: 10/12/11 <coda> -- write tests for ConfiguredCommand
// TODO: 10/12/11 <coda> -- write docs for ConfiguredCommand
// REVIEW: 11/9/11 <coda> -- figure out if there's a way to not pass the config class around

public abstract class ConfiguredCommand<T extends Configuration> extends Command {
    private final ConfigurationFactory<T> factory;

    protected ConfiguredCommand(Class<T> configurationClass,
                                String name) {
        this(configurationClass, name, null);
    }

    protected ConfiguredCommand(Class<T> configurationClass,
                                String name,
                                String description) {
        super(name, description);
        this.factory = ConfigurationFactory.forClass(configurationClass, new Validator());
    }

    protected Optional<String> getConfiguredSyntax() {
        return Optional.absent();
    }

    @Override
    protected final String getSyntax() {
        final StringBuilder syntax = new StringBuilder("<config file>");
        final Optional<String> configured = getConfiguredSyntax();
        if (configured.isPresent() && !configured.get().isEmpty()) {
            syntax.append(' ').append(configured);
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
