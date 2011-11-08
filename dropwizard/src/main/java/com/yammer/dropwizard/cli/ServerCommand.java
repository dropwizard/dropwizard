package com.yammer.dropwizard.cli;

import com.google.common.base.Optional;
import com.yammer.dropwizard.Service;
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
    protected final void run(Service<T> service,
                             T config,
                             CommandLine params) throws Exception {
        new LoggingFactory(config.getLoggingConfiguration()).configure();
        final Environment environment = new Environment();
        service.configure(config, environment);

        final Server server = new ServerFactory(config.getHttpConfiguration()).buildServer(environment);

        final Logger logger = LoggerFactory.getLogger(ServerCommand.class);
        logger.info("Starting " + service.getName());

        if (service.getBanner().isPresent()) {
            logger.info("\n" + service.getBanner().get() + "\n");
        }

        try {
            server.start();
            server.join();
        } catch (Exception e) {
            logger.error("Unable to start server, shutting down");
            server.stop();
        }
    }
}
