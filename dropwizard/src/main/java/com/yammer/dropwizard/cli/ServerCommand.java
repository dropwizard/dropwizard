package com.yammer.dropwizard.cli;

import com.google.common.base.Optional;
import com.yammer.dropwizard.Module;
import com.yammer.dropwizard.AbstractService;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.config.LoggingFactory;
import com.yammer.dropwizard.config.ServerFactory;
import org.apache.commons.cli.CommandLine;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// TODO: 10/12/11 <coda> -- write tests for ServerCommand
// TODO: 10/12/11 <coda> -- write docs for ServerCommand

public class ServerCommand<T extends Configuration> extends ConfiguredCommand<T> {
    public ServerCommand(Class<T> configurationClass) {
        super(configurationClass, "server", "Starts an HTTP server running the service");
    }

    @Override
    protected final Optional<String> getConfiguredSyntax() {
        return Optional.absent();
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

        final Server server = new ServerFactory(configuration.getHttpConfiguration()).buildServer(environment);

        final Logger logger = LoggerFactory.getLogger(ServerCommand.class);
        logger.info("Starting " + service.getName());

        if (service.hasBanner()) {
            logger.info('\n' + service.getBanner() + '\n');
        }

        try {
            server.start();
            server.join();
        } catch (Exception e) {
            logger.error("Unable to start server, shutting down", e);
            server.stop();
        }
    }
}
