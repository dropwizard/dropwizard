package com.codahale.dropwizard.setup;

import com.codahale.dropwizard.jersey.DropwizardResourceConfig;
import com.codahale.dropwizard.jersey.setup.JerseyContainerHolder;
import com.codahale.dropwizard.jersey.setup.JerseyEnvironment;
import com.codahale.dropwizard.jetty.MutableServletContextHandler;
import com.codahale.dropwizard.jetty.setup.ServletEnvironment;
import com.codahale.dropwizard.lifecycle.setup.LifecycleEnvironment;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.spi.container.servlet.ServletContainer;

import javax.validation.Validator;

import static com.google.common.base.Preconditions.checkNotNull;

// TODO: 5/15/13 <coda> -- add tests for Environment
// TODO: 5/15/13 <coda> -- add docs for Environment

/**
 * A Dropwizard application's environment.
 */
public class Environment {
    private final String name;
    private final MetricRegistry metricRegistry;
    private final HealthCheckRegistry healthCheckRegistry;

    private final ObjectMapper objectMapper;
    private Validator validator;

    private final JerseyContainerHolder jerseyServletContainer;
    private final JerseyEnvironment jerseyEnvironment;

    private final MutableServletContextHandler servletContext;
    private final ServletEnvironment servletEnvironment;

    private final LifecycleEnvironment lifecycleEnvironment;

    private final MutableServletContextHandler adminContext;
    private final AdminEnvironment adminEnvironment;

    /**
     * Creates a new environment.
     *
     * @param name                the name of the application
     * @param objectMapper the {@link ObjectMapper} for the application
     */
    public Environment(String name,
                       ObjectMapper objectMapper,
                       Validator validator,
                       MetricRegistry metricRegistry,
                       ClassLoader classLoader) {
        this.name = name;
        this.objectMapper = objectMapper;
        this.metricRegistry = metricRegistry;
        this.healthCheckRegistry = new HealthCheckRegistry();
        this.validator = validator;

        this.servletContext = new MutableServletContextHandler();
        servletContext.setClassLoader(classLoader);
        this.servletEnvironment = new ServletEnvironment(servletContext);

        this.adminContext = new MutableServletContextHandler();
        adminContext.setClassLoader(classLoader);
        this.adminEnvironment = new AdminEnvironment(adminContext, healthCheckRegistry);

        this.lifecycleEnvironment = new LifecycleEnvironment();

        final DropwizardResourceConfig jerseyConfig = new DropwizardResourceConfig(metricRegistry);
        this.jerseyServletContainer = new JerseyContainerHolder(new ServletContainer(jerseyConfig));
        this.jerseyEnvironment = new JerseyEnvironment(jerseyServletContainer, jerseyConfig);
    }

    public JerseyEnvironment jersey() {
        return jerseyEnvironment;
    }

    public AdminEnvironment admin() {
        return adminEnvironment;
    }

    public LifecycleEnvironment lifecycle() {
        return lifecycleEnvironment;
    }

    public ServletEnvironment servlets() {
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

    public MetricRegistry metrics() {
        return metricRegistry;
    }

    public HealthCheckRegistry healthChecks() {
        return healthCheckRegistry;
    }

    /*
    * Internal Accessors
    */

    // TODO: 5/4/13 <coda> -- figure out how to make these accessors not a public API

    public MutableServletContextHandler getApplicationContext() {
        return servletContext;
    }

    public ServletContainer getJerseyServletContainer() {
        return jerseyServletContainer.getContainer();
    }

    public MutableServletContextHandler getAdminContext() {
        return adminContext;
    }
}
