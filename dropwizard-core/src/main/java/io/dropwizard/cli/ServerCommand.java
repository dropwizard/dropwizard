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
        this(application, "server", "Runs the Dropwizard application as an HTTP server");
    }

    /**
     * A constructor to allow reuse of the server command as a different name
     * @param application the application using this command
     * @param name the argument name to invoke this command
     * @param description a summary of what the command does
     */
    protected ServerCommand(final Application<T> application, final String name, final String description) {
        super(application, name, description);
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
            try {
                server.stop();
            } catch (Exception e1) {
                LOGGER.warn("Failure during stop server", e1);
            }
            try {
                cleanup();
            } catch (Exception e2) {
                LOGGER.warn("Failure during cleanup", e2);
            }
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
