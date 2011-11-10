package com.yammer.dropwizard.cli;

import com.yammer.dropwizard.AbstractService;
import com.yammer.dropwizard.Module;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.config.LoggingFactory;
import org.apache.commons.cli.CommandLine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class ManagedCommand<T extends Configuration> extends ConfiguredCommand<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ManagedCommand.class);

    protected ManagedCommand(Class<T> configurationClass,
                             String name) {
        super(configurationClass, name);
    }

    protected ManagedCommand(Class<T> configurationClass,
                             String name,
                             String description) {
        super(configurationClass, name, description);
    }

    @Override
    protected final void run(AbstractService<T> service,
                             T configuration,
                             CommandLine params) throws Exception {
        new LoggingFactory(configuration.getLoggingConfiguration()).configure();
        final Environment environment = new Environment();
        for (Module module : service.getModules()) {
            module.initialize(environment);
        }
        service.initialize(configuration, environment);
        LOGGER.info("Starting " + service.getName());
        environment.start();
        try {
            run(configuration, params);
        } finally {
            environment.stop();
        }
    }

    protected abstract void run(Configuration configuration,
                                CommandLine params) throws Exception;
}
