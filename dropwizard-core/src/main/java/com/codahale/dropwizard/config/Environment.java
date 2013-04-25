package com.codahale.dropwizard.config;

import com.codahale.dropwizard.jersey.DropwizardResourceConfig;
import com.codahale.dropwizard.setup.AdminEnvironment;
import com.codahale.dropwizard.setup.JerseyEnvironment;
import com.codahale.dropwizard.setup.LifecycleEnvironment;
import com.codahale.dropwizard.setup.ServletEnvironment;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Lists;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.component.LifeCycle;

import javax.validation.Validator;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

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

    private final AtomicReference<ServletContainer> jerseyServletContainer;
    private final JerseyEnvironment jerseyEnvironment;

    private final ServletContextHandler servletContext;
    private final ServletEnvironment servletEnvironment;

    private final List<Object> managedObjects;
    private final List<LifeCycle.Listener> lifecycleListeners;
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
        final DropwizardResourceConfig jerseyConfig = new DropwizardResourceConfig(false,
                                                                                   metricRegistry);

        this.servletContext = new ServletContextHandler();
        this.servletEnvironment = new ServletEnvironment(servletContext);

        this.adminContext = new ServletContextHandler();
        this.adminEnvironment = new AdminEnvironment(adminContext, healthCheckRegistry);

        this.managedObjects = Lists.newArrayList();
        this.lifecycleListeners = Lists.newArrayList();
        this.lifecycleEnvironment = new LifecycleEnvironment(managedObjects, lifecycleListeners);

        this.jerseyServletContainer = new AtomicReference<>(new ServletContainer(jerseyConfig));
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
        return jerseyServletContainer.get();
    }

    ServletContextHandler getAdminContext() {
        return adminContext;
    }

    List<Object> getManagedObjects() {
        return managedObjects;
    }

    List<LifeCycle.Listener> getLifecycleListeners() {
        return lifecycleListeners;
    }
}
