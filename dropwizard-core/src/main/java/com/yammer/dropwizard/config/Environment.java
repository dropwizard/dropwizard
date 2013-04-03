package com.yammer.dropwizard.config;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import com.yammer.dropwizard.jersey.DropwizardResourceConfig;
import com.yammer.dropwizard.json.ObjectMapperFactory;
import com.yammer.dropwizard.setup.*;
import com.yammer.dropwizard.validation.Validator;
import com.yammer.metrics.core.HealthCheck;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import static com.google.common.base.Preconditions.checkNotNull;

/**
 * A Dropwizard service's environment.
 */
public class Environment {
    private final String name;
    private final Set<HealthCheck> healthChecks;

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
                       Validator validator) {
        this.name = name;
        this.jsonEnvironment = new JsonEnvironment(objectMapperFactory);
        this.validator = validator;
        final DropwizardResourceConfig jerseyConfig = new DropwizardResourceConfig(false);

        this.server = new Server();

        this.servletContext = new ServletContextHandler();
        this.servletEnvironment = new ServletEnvironment(servletContext);

        this.healthChecks = Sets.newHashSet();
        this.adminContext = new ServletContextHandler();
        this.adminEnvironment = new AdminEnvironment(adminContext, healthChecks);

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

    /*
    * Internal Accessors
    */

    ImmutableSet<HealthCheck> getHealthChecks() {
        return ImmutableSet.copyOf(healthChecks);
    }

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
