package com.codahale.dropwizard.cli;

import com.codahale.dropwizard.Configuration;
import com.codahale.dropwizard.Service;
import com.codahale.dropwizard.config.Environment;
import com.codahale.dropwizard.config.ServerFactory;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import net.sourceforge.argparse4j.inf.Namespace;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

// TODO: 10/12/11 <coda> -- write tests for ServerCommand

/**
 * Runs a service as an HTTP server.
 *
 * @param <T> the {@link Configuration} subclass which is loaded from the configuration file
 */
public class ServerCommand<T extends Configuration> extends EnvironmentCommand<T> {
    private final Class<T> configurationClass;

    public ServerCommand(Service<T> service) {
        super(service, "server", "Runs the Dropwizard service as an HTTP server");
        this.configurationClass = service.getConfigurationClass();
    }

    /*
     * Since we don't subclass ServerCommand, we need a concrete reference to the configuration
     * class.
     */
    @Override
    protected Class<T> getConfigurationClass() {
        return configurationClass;
    }

    @Override
    protected void run(Environment environment, Namespace namespace, T configuration) throws Exception {
        final Server server = new ServerFactory(configuration.getServerConfiguration(),
                                                environment.getName()).buildServer(environment);
        final Logger logger = LoggerFactory.getLogger(ServerCommand.class);
        logBanner(environment.getName(), logger);
        try {
            server.start();
        } catch (Exception e) {
            logger.error("Unable to start server, shutting down", e);
            server.stop();
        }
    }

    private void logBanner(String name, Logger logger) {
        try {
            final String banner = Resources.toString(Resources.getResource("banner.txt"),
                                                     Charsets.UTF_8);
            logger.info("Starting {}\n{}", name, banner);
        } catch (IllegalArgumentException | IOException ignored) {
            // don't display the banner if there isn't one
            logger.info("Starting {}", name);
        }
    }
}
