package com.yammer.dropwizard.cli;

import com.yammer.dropwizard.AbstractService;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.config.LoggingFactory;
import com.yammer.dropwizard.logging.Log;
import org.apache.commons.cli.CommandLine;

public abstract class ManagedCommand<T extends Configuration> extends ConfiguredCommand<T> {
    private static final Log LOG = Log.forClass(ManagedCommand.class);

    protected ManagedCommand(String name,
                             String description) {
        super(name, description);
    }

    @Override
    protected final void run(AbstractService<T> service,
                             T configuration,
                             CommandLine params) throws Exception {
        new LoggingFactory(configuration.getLoggingConfiguration()).configure();
        final Environment environment = new Environment(configuration, service);
        service.initializeWithBundles(configuration, environment);
        LOG.info("Starting {}", service.getName());
        environment.start();
        try {
            run(configuration, environment, params);
        } finally {
            environment.stop();
        }
    }

    protected abstract void run(T configuration,
                                Environment environment,
                                CommandLine params) throws Exception;
}
