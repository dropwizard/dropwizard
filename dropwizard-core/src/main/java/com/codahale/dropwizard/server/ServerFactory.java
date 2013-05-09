package com.codahale.dropwizard.server;

import com.codahale.dropwizard.jersey.setup.JerseyEnvironment;
import com.codahale.dropwizard.lifecycle.setup.LifecycleEnvironment;
import com.codahale.dropwizard.util.Subtyped;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;

import javax.annotation.Nullable;
import javax.validation.Validator;

/**
 * A factory for building {@link Server} instances for Dropwizard applications.
 *
 * @see DefaultServerFactory
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
              include = JsonTypeInfo.As.PROPERTY,
              property = "type",
              defaultImpl = DefaultServerFactory.class)
public interface ServerFactory extends Subtyped {
    /**
     * Build a server for the given Dropwizard application.
     *
     * @param name                  the application's name
     * @param metricRegistry        the application's metrics
     * @param healthChecks          the application's health checks
     * @param lifecycle             the application's lifecycle environment
     * @param applicationContext    the application's application servlet context
     * @param jerseyContainer       the application's Jersey container
     * @param adminContext          the application's administrative servlet context
     * @param jersey                the application's Jersey environment
     * @param objectMapper          the application's {@link ObjectMapper}
     * @param validator             the application's {@link Validator}
     * @return a {@link Server} running the Dropwizard application
     */
    Server build(String name,
                 MetricRegistry metricRegistry,
                 HealthCheckRegistry healthChecks,
                 LifecycleEnvironment lifecycle,
                 ServletContextHandler applicationContext,
                 @Nullable ServletContainer jerseyContainer,
                 ServletContextHandler adminContext,
                 JerseyEnvironment jersey,
                 ObjectMapper objectMapper,
                 Validator validator);
}
