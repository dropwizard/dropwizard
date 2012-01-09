package com.yammer.dropwizard.cli;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.yammer.dropwizard.AbstractService;
import com.yammer.dropwizard.config.Configuration;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.config.ServerFactory;
import com.yammer.dropwizard.logging.Log;
import org.apache.commons.cli.CommandLine;
import org.eclipse.jetty.server.Server;

// TODO: 10/12/11 <coda> -- write tests for ServerCommand

/**
 * Runs a service as an HTTP server.
 *
 * @param <T> the {@link Configuration} subclass which is loaded from the configuration file
 */
public class ServerCommand<T extends Configuration> extends ConfiguredCommand<T> {
    private final Class<T> configurationClass;

    /**
     * Creates a new {@link ServerCommand} with the given configuration class.
     *
     * @param configurationClass    the configuration class the YAML file is parsed as
     */
    public ServerCommand(Class<T> configurationClass) {
        super("server", "Starts an HTTP server running the service");
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
    protected void run(AbstractService<T> service,
                       T configuration,
                       CommandLine params) throws Exception {
        final Environment environment = new Environment();
        service.initializeWithBundles(configuration, environment);

        final Server server = new ServerFactory(configuration.getHttpConfiguration()).buildServer(environment);

        final Log log = Log.forClass(ServerCommand.class);
        log.info("Starting {}", service.getName());

        try {
            log.info("\n{}", Resources.toString(Resources.getResource("banner.txt"), Charsets.UTF_8));
        } catch (IllegalArgumentException ignored) {
            // don't display the banner if there isn't one
        }

        try {
            server.start();
            server.join();
        } catch (Exception e) {
            log.error(e, "Unable to start server, shutting down");
            server.stop();
        }
    }
}
