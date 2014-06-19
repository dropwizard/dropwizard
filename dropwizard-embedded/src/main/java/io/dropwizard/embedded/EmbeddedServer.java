package io.dropwizard.embedded;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import io.dropwizard.Configuration;

import io.dropwizard.configuration.ConfigurationException;
import io.dropwizard.configuration.ConfigurationFactory;
import io.dropwizard.configuration.ConfigurationFactoryFactory;
import io.dropwizard.configuration.ConfigurationSourceProvider;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.server.Server;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import javax.validation.Validator;

/**
 * Runs the HTTP server behind an embedded Dropwizard Service.
 * This class is a facade to replace io.dropwizard.cli.ServerCommand and all of Dropwizard's CLI
 * In this way it makes Service easily embeddable into an existing daemon service.
 *
 * @param <T> the {@link io.dropwizard.Configuration} subclass which is loaded from the configuration file
 */
public class EmbeddedServer<T extends Configuration> {
    private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddedServer.class);

    private final Service<T> service;
    private final Class<T> configurationClass;

    private Server server;
    private Environment environment;

    public EmbeddedServer(Service<T> service) {
        this.service = service;
        this.configurationClass = service.getConfigurationClass();
    }

    public final void start(EmbeddedBootstrap<T> bootstrap, String configFile) throws Exception {
        T configuration = parseConfiguration(bootstrap.getConfigurationFactoryFactory(),
                                            bootstrap.getConfigurationSourceProvider(),
                                            bootstrap.getValidatorFactory().getValidator(),
                                            configFile,
                                            configurationClass,
                                            bootstrap.getObjectMapper());
        start(bootstrap, configuration);
    }

    public final void start(EmbeddedBootstrap<T> bootstrap, T configuration) throws Exception {
        environment = new Environment(bootstrap.getName(),
                                        bootstrap.getObjectMapper(),
                                        bootstrap.getValidatorFactory().getValidator(),
                                        bootstrap.getMetricRegistry(),
                                        bootstrap.getClassLoader());
        configuration.getMetricsFactory().configure(environment.lifecycle(),
                                                    bootstrap.getMetricRegistry());
        bootstrap.run(configuration, environment);
        service.start(configuration, environment);
        startServer(configuration);
    }

    private void startServer(T configuration) throws Exception {
        this.server = configuration.getServerFactory().build(environment);
        try {
            server.start();
        } catch (Exception e) {
            LOGGER.error("Unable to start server, shutting down", e);
            server.stop();
        }
    }

    public final void stop() throws Exception {
        if (server == null) {
            LOGGER.error("Attempted to stop server, but was never created");
            return;
        } else if (server.isStopped()) {
            LOGGER.error("Attempted to stop server, but was already stopped");
            return;
        }

        try {
            server.stop();

            if (!server.isStopped() || server.getThreadPool().getThreads() > 0) {
                throw new Exception(
                        String.format("Failed to stop server after graceful shutdown period, still %d threads alive",
                                    server.getThreadPool().getThreads()));
            }

            service.stop(environment);
        } catch (Exception e) {
            LOGGER.error("Failed to stop server", e);
            throw e;
        }
    }

    protected T parseConfiguration(ConfigurationFactoryFactory<T> configurationFactoryFactory,
                                       ConfigurationSourceProvider provider,
                                       Validator validator,
                                       String path,
                                       Class<T> klass,
                                       ObjectMapper objectMapper) throws IOException, ConfigurationException {
        final ConfigurationFactory<T> configurationFactory =
                configurationFactoryFactory.create(klass, validator, objectMapper, "dw");
        if (path != null) {
            return configurationFactory.build(provider, path);
        }
        return configurationFactory.build();
    }
}
