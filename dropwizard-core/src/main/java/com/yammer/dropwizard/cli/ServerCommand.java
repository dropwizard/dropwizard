package com.yammer.dropwizard.cli;

import com.beust.jcommander.Parameters;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.yammer.dropwizard.AbstractService;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.config.ServerFactory;
import com.yammer.dropwizard.logging.Log;
import org.eclipse.jetty.server.Server;

import java.io.IOException;

// TODO: 10/12/11 <coda> -- write tests for ServerCommand

/**
 * Runs a service as an HTTP server.
 *
 * @param <T> the {@link Configuration} subclass which is loaded from the configuration file
 */
@Parameters(commandNames = "server",
            commandDescription = "Run as an HTTP server")
public class ServerCommand<T extends Configuration> extends ConfiguredCommand<T> {
    private final Class<T> configurationClass;

    /**
     * Creates a new {@link ServerCommand} with the given configuration class.
     *
     * @param configurationClass    the configuration class the YAML file is parsed as
     */
    public ServerCommand(Class<T> configurationClass) {
        this.configurationClass = configurationClass;
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
    protected void run(AbstractService<T> service, T configuration) throws Exception {
        final Environment environment = new Environment(service, configuration);
        service.initializeWithBundles(configuration, environment);
        final Server server = new ServerFactory(configuration.getHttpConfiguration(),
                                                service.getName()).buildServer(environment);
        final Log log = Log.forClass(ServerCommand.class);
        logBanner(service, log);
        try {
            server.start();
            server.join();
        } catch (Exception e) {
            log.error(e, "Unable to start server, shutting down");
            server.stop();
        }
    }

    private void logBanner(AbstractService<T> service, Log log) {
        try {
            final String banner = Resources.toString(Resources.getResource("banner.txt"),
                                                     Charsets.UTF_8);
            log.info("Starting {}\n{}", service.getName(), banner);
        } catch (IllegalArgumentException ignored) {
            // don't display the banner if there isn't one
            log.info("Starting {}", service.getName());
        } catch (IOException ignored) {
            log.info("Starting {}", service.getName());
        }
    }
}
