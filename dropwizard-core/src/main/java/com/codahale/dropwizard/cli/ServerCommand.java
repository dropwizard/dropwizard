package com.codahale.dropwizard.cli;

import com.codahale.dropwizard.Configuration;
import com.codahale.dropwizard.Service;
import com.codahale.dropwizard.setup.Environment;
import com.codahale.metrics.health.jvm.ThreadDeadlockHealthCheck;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import net.sourceforge.argparse4j.inf.Namespace;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.regex.Pattern;

// TODO: 10/12/11 <coda> -- write tests for ServerCommand

/**
 * Runs a service as an HTTP server.
 *
 * @param <T> the {@link com.codahale.dropwizard.Configuration} subclass which is loaded from the configuration file
 */
public class ServerCommand<T extends Configuration> extends EnvironmentCommand<T> {
    private static final Pattern WINDOWS_NEWLINE = Pattern.compile("\\r\\n?");

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
        environment.healthChecks().register("deadlocks", new ThreadDeadlockHealthCheck());
        final Server server = configuration.getServerFactory().build(environment.getName(),
                                                                     environment.metrics(),
                                                                     environment.healthChecks(),
                                                                     environment.lifecycle(),
                                                                     environment.getServletContext(),
                                                                     environment.getJerseyServletContainer(),
                                                                     environment.getAdminContext(),
                                                                     environment.jersey(),
                                                                     environment.getObjectMapper(),
                                                                     environment.getValidator());
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
            logger.info(String.format("Starting {}%n{}"), name, normalize(banner));
        } catch (IllegalArgumentException | IOException ignored) {
            // don't display the banner if there isn't one
            logger.info("Starting {}", name);
        }
    }

    private String normalize(String s) {
        return WINDOWS_NEWLINE.matcher(s).replaceAll("\n").replace("\n", String.format("%n"));
    }
}
