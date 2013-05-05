package com.codahale.dropwizard.server;

import com.codahale.dropwizard.jersey.setup.JerseyEnvironment;
import com.codahale.dropwizard.jetty.ConnectorFactory;
import com.codahale.dropwizard.jetty.ContextRoutingHandler;
import com.codahale.dropwizard.jetty.HttpConnectorFactory;
import com.codahale.dropwizard.lifecycle.setup.LifecycleEnvironment;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheckRegistry;
import com.codahale.metrics.jetty9.InstrumentedHandler;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sun.jersey.spi.container.servlet.ServletContainer;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.RequestLogHandler;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.hibernate.validator.constraints.NotEmpty;

import javax.validation.Valid;
import javax.validation.Validator;
import javax.validation.constraints.NotNull;

@JsonTypeName("simple")
public class SimpleServerFactory extends AbstractServerFactory {
    @Valid
    @NotNull
    private ConnectorFactory connector = HttpConnectorFactory.application();

    @NotEmpty
    private String serviceRoot = "/service";

    @NotEmpty
    private String adminRoot = "/admin";

    @JsonProperty
    public ConnectorFactory getConnector() {
        return connector;
    }

    @JsonProperty
    public void setConnector(ConnectorFactory factory) {
        this.connector = factory;
    }

    @JsonProperty
    public String getServiceRoot() {
        return serviceRoot;
    }

    @JsonProperty
    public void setServiceRoot(String root) {
        this.serviceRoot = root;
    }

    @JsonProperty
    public String getAdminRoot() {
        return adminRoot;
    }

    @JsonProperty
    public void setAdminRoot(String root) {
        this.adminRoot = root;
    }

    @Override
    public Server build(String name,
                        MetricRegistry metricRegistry,
                        HealthCheckRegistry healthChecks,
                        LifecycleEnvironment lifecycle,
                        ServletContextHandler applicationContext,
                        ServletContainer jerseyContainer,
                        ServletContextHandler adminContext,
                        JerseyEnvironment jersey,
                        ObjectMapper objectMapper,
                        Validator validator) {

        final ThreadPool threadPool = createThreadPool(metricRegistry);
        final Server server = buildServer(lifecycle, threadPool);

        final ServletContextHandler applicationHandler = createExternalServlet(jersey,
                                                                               objectMapper,
                                                                               validator,
                                                                               applicationContext,
                                                                               jerseyContainer);
        applicationHandler.setContextPath(serviceRoot);

        final ServletContextHandler adminHandler = createInternalServlet(adminContext,
                                                                         metricRegistry,
                                                                         healthChecks);
        adminHandler.setContextPath(adminRoot);

        final Connector conn = connector.build(server, metricRegistry, name);
        server.addConnector(conn);

        final ContextRoutingHandler routingHandler = new ContextRoutingHandler(applicationHandler,
                                                                               adminHandler);
        final Handler gzipHandler = getGzipHandlerFactory().wrapHandler(routingHandler);
        final Handler handler = new InstrumentedHandler(metricRegistry, gzipHandler);

        if (getRequestLogFactory().isEnabled()) {
            final RequestLogHandler requestLogHandler = getRequestLogFactory().build(name);
            requestLogHandler.setHandler(handler);
            server.setHandler(requestLogHandler);
        } else {
            server.setHandler(handler);
        }

        return server;
    }
}
