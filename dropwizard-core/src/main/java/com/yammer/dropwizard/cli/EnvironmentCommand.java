package com.yammer.dropwizard.cli;

import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.config.LoggingFactory;

public abstract class EnvironmentCommand<T extends Configuration> extends ConfiguredCommand<T> {
    private final Service<T> service;

    protected EnvironmentCommand(Service<T> service) {
        this.service = service;
    }

    @Override
    protected void run(Bootstrap<T> bootstrap, T configuration) throws Exception {
        final Environment environment = new Environment(bootstrap.getName(),
                                                        configuration,
                                                        bootstrap.getObjectMapperFactory().clone());
        bootstrap.runWithBundles(service, configuration, environment);
        new LoggingFactory(configuration.getLoggingConfiguration(),
                           bootstrap.getName()).configure();
        run(environment, configuration);
    }

    protected abstract void run(Environment environment, T configuration) throws Exception;
}
