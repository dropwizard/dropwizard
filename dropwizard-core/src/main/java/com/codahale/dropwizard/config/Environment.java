package com.codahale.dropwizard.config;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.codahale.dropwizard.jersey.DropwizardResourceConfig;
import com.codahale.dropwizard.json.ObjectMapperFactory;
import com.codahale.dropwizard.setup.*;
import com.codahale.dropwizard.validation.Validator;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A Dropwizard service's environment.
 */
public class Environment {
    private final String name;
    private final MetricRegistry metricRegistry;
    private final HealthCheckRegistry healthCheckRegistry;

    private final JsonEnvironment jsonEnvironment;
    private Validator validator;

    private final AtomicReference<ServletContainer> jerseyServletContainer;
    private final JerseyEnvironment jerseyEnvironment;

    private final ServletContextHandler servletContext;
    private final ServletEnvironment servletEnvironment;

    private final Server server;
    private final LifecycleEnvironment lifecycleEnvironment;

    private final ServletContextHandler adminContext;
    private final AdminEnvironment adminEnvironment;

    /**
     * Creates a new environment.
     *
     * @param name                the name of the service
     * @param objectMapperFactory the {@link ObjectMapperFactory} for the service
     */
    public Environment(String name,
                       ObjectMapperFactory objectMapperFactory,
                       Validator validator,
                       MetricRegistry metricRegistry) {
        this.name = name;
        this.metricRegistry = metricRegistry;
        this.healthCheckRegistry = new HealthCheckRegistry();
        this.jsonEnvironment = new JsonEnvironment(objectMapperFactory);
        this.validator = validator;
        final DropwizardResourceConfig jerseyConfig = new DropwizardResourceConfig(false,
                                                                                   metricRegistry);

        this.server = new Server();

        this.servletContext = new ServletContextHandler();
        this.servletEnvironment = new ServletEnvironment(servletContext);

        this.adminContext = new ServletContextHandler();
        this.adminEnvironment = new AdminEnvironment(adminContext, healthCheckRegistry);

        this.lifecycleEnvironment = new LifecycleEnvironment(server);

        this.jerseyServletContainer = new AtomicReference<ServletContainer>(new ServletContainer(jerseyConfig));
        this.jerseyEnvironment = new JerseyEnvironment(servletContext, jerseyServletContainer,
                                                       jerseyConfig);
    }

    public JerseyEnvironment getJerseyEnvironment() {
        return jerseyEnvironment;
    }

    public AdminEnvironment getAdminEnvironment() {
        return adminEnvironment;
    }

    public LifecycleEnvironment getLifecycleEnvironment() {
        return lifecycleEnvironment;
    }

    public ServletEnvironment getServletEnvironment() {
        return servletEnvironment;
    }

    public JsonEnvironment getJsonEnvironment() {
        return jsonEnvironment;
    }

    public String getName() {
        return name;
    }

    public Validator getValidator() {
        return validator;
    }

    public void setValidator(Validator validator) {
        this.validator = checkNotNull(validator);
    }

    public MetricRegistry getMetricRegistry() {
        return metricRegistry;
    }

    public HealthCheckRegistry getHealthCheckRegistry() {
        return healthCheckRegistry;
    }

    /*
    * Internal Accessors
    */

    ServletContextHandler getServletContext() {
        return servletContext;
    }

    ServletContainer getJerseyServletContainer() {
        return jerseyServletContainer.get();
    }

    Server getServer() {
        return server;
    }

    ServletContextHandler getAdminContext() {
        return adminContext;
    }
}
