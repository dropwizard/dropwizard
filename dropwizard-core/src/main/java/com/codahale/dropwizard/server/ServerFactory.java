package com.codahale.dropwizard.server;

import com.codahale.dropwizard.jersey.setup.JerseyEnvironment;
import com.codahale.dropwizard.lifecycle.setup.LifecycleEnvironment;
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
 * A factory for building {@link Server} instances for Dropwizard services.
 *
 * @see DefaultServerFactory
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
              include = JsonTypeInfo.As.PROPERTY,
              property = "type",
              defaultImpl = DefaultServerFactory.class)
public interface ServerFactory {
    /**
     * Build a server for the given Dropwizard service.
     *
     * @param name                  the service's name
     * @param metricRegistry        the service's metrics
     * @param healthChecks          the service's health checks
     * @param lifecycle             the service's lifecycle environment
     * @param applicationContext    the service's application servlet context
     * @param jerseyContainer       the service's Jersey container
     * @param adminContext          the service's administrative servlet context
     * @param jersey                the service's Jersey environment
     * @param objectMapper          the service's {@link ObjectMapper}
     * @param validator             the service's {@link Validator}
     * @return a {@link Server} running the Dropwizard service
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
