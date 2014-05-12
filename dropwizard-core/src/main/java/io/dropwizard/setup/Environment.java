package io.dropwizard.setup;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import io.dropwizard.errors.EarlyEofExceptionMapper;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.jersey.setup.JerseyContainerHolder;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.jetty.MutableServletContextHandler;
import io.dropwizard.jetty.setup.ServletEnvironment;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;

import javax.validation.Validator;

import static com.google.common.base.Preconditions.checkNotNull;

// TODO: 5/15/13 <coda> -- add tests for Environment

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
        this.adminEnvironment = new AdminEnvironment(adminContext, healthCheckRegistry, metricRegistry);

        this.lifecycleEnvironment = new LifecycleEnvironment();

        final DropwizardResourceConfig jerseyConfig = new DropwizardResourceConfig(metricRegistry);

        jerseyConfig.getSingletons().add(new EarlyEofExceptionMapper());

        this.jerseyServletContainer = new JerseyContainerHolder(new ServletContainer(jerseyConfig));
        this.jerseyEnvironment = new JerseyEnvironment(jerseyServletContainer, jerseyConfig);
    }

    /**
     * Returns the application's {@link JerseyEnvironment}.
     */
    public JerseyEnvironment jersey() {
        return jerseyEnvironment;
    }

    /**
     * Returns the application's {@link AdminEnvironment}.
     */
    public AdminEnvironment admin() {
        return adminEnvironment;
    }

    /**
     * Returns the application's {@link LifecycleEnvironment}.
     */
    public LifecycleEnvironment lifecycle() {
        return lifecycleEnvironment;
    }

    /**
     * Returns the application's {@link ServletEnvironment}.
     */
    public ServletEnvironment servlets() {
        return servletEnvironment;
    }

    /**
     * Returns the application's {@link ObjectMapper}.
     */
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    /**
     * Returns the application's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the application's {@link Validator}.
     */
    public Validator getValidator() {
        return validator;
    }

    /**
     * Sets the application's {@link Validator}.
     */
    public void setValidator(Validator validator) {
        this.validator = checkNotNull(validator);
    }

    /**
     * Returns the application's {@link MetricRegistry}.
     */
    public MetricRegistry metrics() {
        return metricRegistry;
    }

    /**
     * Returns the application's {@link HealthCheckRegistry}.
     */
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
