package io.dropwizard.setup;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.jersey.setup.JerseyContainerHolder;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.jersey.setup.JerseyServletContainer;
import io.dropwizard.jetty.MutableServletContextHandler;
import io.dropwizard.jetty.setup.ServletEnvironment;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;

import javax.servlet.Servlet;
import javax.validation.Validator;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.ThreadPoolExecutor;

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
    
    private final ExecutorService healthCheckExecutorService;

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

        this.jerseyServletContainer = new JerseyContainerHolder(new JerseyServletContainer(jerseyConfig));
        this.jerseyEnvironment = new JerseyEnvironment(jerseyServletContainer, jerseyConfig);


        this.healthCheckExecutorService = this.lifecycle().executorService("TimeBoundHealthCheck-pool-%d")
                .workQueue(new ArrayBlockingQueue<Runnable>(1))
                .minThreads(1)
                .maxThreads(4)
                .threadFactory(new ThreadFactoryBuilder().setDaemon(true).build())
                .rejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy())
                .build();
    }

    /**
     * Returns the application's {@link JerseyEnvironment}.
     */
    public JerseyEnvironment jersey() {
        return jerseyEnvironment;
    }

    /**
     * Returns an {@link ExecutorService} to run time bound health checks
     */
    public ExecutorService getHealthCheckExecutorService() {
        return healthCheckExecutorService;
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

    public Servlet getJerseyServletContainer() {
        return jerseyServletContainer.getContainer();
    }

    public MutableServletContextHandler getAdminContext() {
        return adminContext;
    }
}
