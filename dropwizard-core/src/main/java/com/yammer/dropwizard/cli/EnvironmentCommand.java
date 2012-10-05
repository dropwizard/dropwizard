package com.yammer.dropwizard.cli;

import com.yammer.dropwizard.Service;
import com.yammer.dropwizard.config.Bootstrap;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import net.sourceforge.argparse4j.inf.Namespace;

public abstract class EnvironmentCommand<T extends Configuration> extends ConfiguredCommand<T> {
    private final Service<T> service;

    protected EnvironmentCommand(Service<T> service, String name, String description) {
        super(name, description);
        this.service = service;
    }

    @Override
    protected void run(Bootstrap<T> bootstrap, Namespace namespace, T configuration) throws Exception {
        final Environment environment = new Environment(bootstrap.getName(),
                                                        configuration,
                                                        bootstrap.getObjectMapperFactory().clone());
        bootstrap.runWithBundles(service, configuration, environment);
        run(environment, namespace, configuration);
    }

    protected abstract void run(Environment environment, Namespace namespace, T configuration) throws Exception;
}
