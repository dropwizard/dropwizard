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

import javax.validation.Validator;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME,
              include = JsonTypeInfo.As.PROPERTY,
              property = "type",
              defaultImpl = DefaultServerFactory.class)
public interface ServerFactory {
    Server build(String name,
                 MetricRegistry metricRegistry,
                 HealthCheckRegistry healthChecks,
                 LifecycleEnvironment lifecycle,
                 ServletContextHandler applicationContext,
                 ServletContainer jerseyContainer,
                 ServletContextHandler adminContext,
                 JerseyEnvironment jersey,
                 ObjectMapper objectMapper,
                 Validator validator);
}
