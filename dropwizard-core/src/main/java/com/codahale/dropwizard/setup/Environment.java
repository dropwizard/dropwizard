package com.codahale.dropwizard.setup;

import com.codahale.dropwizard.jersey.DropwizardResourceConfig;
import com.codahale.dropwizard.jersey.setup.JerseyContainerHolder;
import com.codahale.dropwizard.jersey.setup.JerseyEnvironment;
import com.codahale.dropwizard.lifecycle.setup.LifecycleEnvironment;
import com.codahale.dropwizard.jetty.setup.ServletEnvironment;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.eclipse.jetty.servlet.ServletContextHandler;

import javax.validation.Validator;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A Dropwizard service's environment.
 */
public class Environment {
    private final String name;
    private final MetricRegistry metricRegistry;
    private final HealthCheckRegistry healthCheckRegistry;

    private final ObjectMapper objectMapper;
    private Validator validator;

    private final JerseyContainerHolder jerseyServletContainer;
    private final JerseyEnvironment jerseyEnvironment;

    private final ServletContextHandler servletContext;
    private final ServletEnvironment servletEnvironment;

    private final LifecycleEnvironment lifecycleEnvironment;

    private final ServletContextHandler adminContext;
    private final AdminEnvironment adminEnvironment;

    /**
     * Creates a new environment.
     *
     * @param name                the name of the service
     * @param objectMapper the {@link ObjectMapper} for the service
     */
    public Environment(String name,
                       ObjectMapper objectMapper,
                       Validator validator,
                       MetricRegistry metricRegistry) {
        this.name = name;
        this.objectMapper = objectMapper;
        this.metricRegistry = metricRegistry;
        this.healthCheckRegistry = new HealthCheckRegistry();
        this.validator = validator;
        final DropwizardResourceConfig jerseyConfig = new DropwizardResourceConfig(metricRegistry);

        this.servletContext = new ServletContextHandler();
        this.servletEnvironment = new ServletEnvironment(servletContext);

        this.adminContext = new ServletContextHandler();
        this.adminEnvironment = new AdminEnvironment(adminContext, healthCheckRegistry);

        this.lifecycleEnvironment = new LifecycleEnvironment();

        this.jerseyServletContainer = new JerseyContainerHolder(new ServletContainer(jerseyConfig));
        this.jerseyEnvironment = new JerseyEnvironment(jerseyServletContainer, jerseyConfig);
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

    public ObjectMapper getObjectMapper() {
        return objectMapper;
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
        return jerseyServletContainer.getContainer();
    }

    ServletContextHandler getAdminContext() {
        return adminContext;
    }
}
