package io.dropwizard.cli;

import io.dropwizard.Application;
import io.dropwizard.Configuration;
import io.dropwizard.setup.Environment;
import net.sourceforge.argparse4j.inf.Namespace;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
import org.eclipse.jetty.util.component.LifeCycle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Runs a application as an HTTP server.
 *
 * @param <T> the {@link Configuration} subclass which is loaded from the configuration file
 */
public class ServerCommand<T extends Configuration> extends EnvironmentCommand<T> {
    private static final Logger LOGGER = LoggerFactory.getLogger(ServerCommand.class);

    private final Class<T> configurationClass;

    public ServerCommand(Application<T> application) {
        super(application, "server", "Runs the Dropwizard application as an HTTP server");
        this.configurationClass = application.getConfigurationClass();
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
        final Server server = configuration.getServerFactory().build(environment);
        try {
            server.addLifeCycleListener(new LifeCycleListener());
            cleanupAsynchronously();
            server.start();
        } catch (Exception e) {
            LOGGER.error("Unable to start server, shutting down", e);
            server.stop();
            cleanup();
            throw e;
        }
    }

    private class LifeCycleListener extends AbstractLifeCycle.AbstractLifeCycleListener {
        @Override
        public void lifeCycleStopped(LifeCycle event) {
            cleanup();
        }
    }
}
