package io.dropwizard.setup;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.jersey.setup.JerseyContainerHolder;
import io.dropwizard.jersey.setup.JerseyEnvironment;
import io.dropwizard.jetty.MutableServletContextHandler;
import io.dropwizard.jetty.setup.ServletEnvironment;
import org.glassfish.jersey.servlet.ServletContainer;

import javax.servlet.Servlet;
import javax.validation.Validator;

public class HttpEnvironment extends Environment {
    private final JerseyContainerHolder jerseyServletContainer;
    private final JerseyEnvironment jerseyEnvironment;
    private final MutableServletContextHandler servletContext;
    private final ServletEnvironment servletEnvironment;
    private final MutableServletContextHandler adminContext;
    private final AdminEnvironment adminEnvironment;

    /**
     * Creates a new environment.
     *
     * @param name           the name of the application
     * @param objectMapper   the {@link ObjectMapper} for the application
     * @param validator
     * @param metricRegistry
     * @param classLoader
     */
    public HttpEnvironment(String name, ObjectMapper objectMapper, Validator validator, MetricRegistry metricRegistry, ClassLoader classLoader) {
        super(name, objectMapper, validator, metricRegistry);

        this.servletContext = new MutableServletContextHandler();
        servletContext.setClassLoader(classLoader);
        this.servletEnvironment = new ServletEnvironment(servletContext);

        this.adminContext = new MutableServletContextHandler();
        adminContext.setClassLoader(classLoader);
        this.adminEnvironment = new AdminEnvironment(adminContext, healthChecks(), metricRegistry);

        final DropwizardResourceConfig jerseyConfig = new DropwizardResourceConfig(metricRegistry);

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
     * Returns the application's {@link ServletEnvironment}.
     */
    public ServletEnvironment servlets() {
        return servletEnvironment;
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
