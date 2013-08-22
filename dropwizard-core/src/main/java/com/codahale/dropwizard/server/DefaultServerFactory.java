package com.codahale.dropwizard.server;

import com.codahale.dropwizard.jetty.ConnectorFactory;
import com.codahale.dropwizard.jetty.DefaultServletHandlerFactory;
import com.codahale.dropwizard.jetty.HttpConnectorFactory;
import com.codahale.dropwizard.jetty.RoutingHandler;
import com.codahale.dropwizard.setup.Environment;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.Maps;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

// TODO: 5/15/13 <coda> -- add tests for DefaultServerFactory

/**
 * The default implementation of {@link ServerFactory}, which allows for multiple sets of
 * application and admin connectors, all running on separate ports. Admin connectors use a separate
 * thread pool to keep the control and data planes separate(ish).
 * <p/>
 * <b>Configuration Parameters:</b>
 * <table>
 *     <tr>
 *         <td>Name</td>
 *         <td>Default</td>
 *         <td>Description</td>
 *     </tr>
 *     <tr>
 *         <td>{@code application}</td>
 *         <td>A {@link DefaultServletHandlerFactory handler} for port 8080.</td>
 *         <td>The handler for the application.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code admin}</td>
 *         <td>A {@link DefaultServletHandlerFactory handler} for port 8081.</td>
 *         <td>The handler for the admin servlets, including metrics and tasks.</td>
 *     </tr>
 * </table>
 * <p/>
 * For more configuration parameters, see {@link AbstractServerFactory}.
 *
 * @see ServerFactory
 * @see AbstractServerFactory
 */
@JsonTypeName("default")
public class DefaultServerFactory extends AbstractServerFactory {

    @Valid
    @NotNull
    private DefaultServletHandlerFactory appHandlerFactory
            = DefaultServletHandlerFactory.forConnectors(HttpConnectorFactory.application());

    @Valid
    @NotNull
    private DefaultServletHandlerFactory adminHandlerFactory
            = DefaultServletHandlerFactory.forConnectors(HttpConnectorFactory.admin());

    @Override
    @JsonProperty("application")
    public DefaultServletHandlerFactory getApplicationHandlerFactory() {
        return appHandlerFactory;
    }

    @JsonProperty("application")
    public void setAppHandlerFactory(DefaultServletHandlerFactory factory) {
        this.appHandlerFactory = factory;
    }

    @Override
    @JsonProperty("admin")
    public DefaultServletHandlerFactory getAdminHandlerFactory() {
        return adminHandlerFactory;
    }

    @JsonProperty("admin")
    public void setAdminHandlerFactory(DefaultServletHandlerFactory factory) {
        this.adminHandlerFactory = factory;
    }

    @Override
    public Server build(Environment environment) {
        printBanner(environment.getName());
        final ThreadPool threadPool = appHandlerFactory.buildThreadPool(environment.metrics(), "dw");
        final Server server = buildServer(environment.lifecycle(), threadPool);
        final Handler applicationHandler = appHandlerFactory.build(
                server,
                environment.getApplicationContext(),
                environment.metrics(),
                environment.getName());
        addJerseyServlet(environment.getApplicationContext(),
                         environment.getJerseyServletContainer(),
                         environment.jersey(),
                         environment.getObjectMapper(),
                         environment.getValidator());

        final Handler adminHandler = adminHandlerFactory.build(
                server,
                environment.getApplicationContext(),
                environment.metrics(),
                environment.getName());

        addAdminServlets(environment.getAdminContext(),
                         environment.metrics(),
                         environment.healthChecks());

        final RoutingHandler routingHandler = buildRoutingHandler(
                environment.metrics(),
                server,
                applicationHandler,
                adminHandler);
        server.setHandler(routingHandler);
        return server;
    }

    private RoutingHandler buildRoutingHandler(MetricRegistry metricRegistry,
                                               Server server,
                                               Handler applicationHandler,
                                               Handler adminHandler) {
        final List<Connector> appConnectors = appHandlerFactory.buildConnectors(
                metricRegistry,
                server,
                server.getThreadPool(),
                "application");

        final List<Connector> adConnectors = adminHandlerFactory.buildConnectors(
                metricRegistry,
                server,
                adminHandlerFactory.buildThreadPool(metricRegistry, "dw-admin"),
                "admin");

        final Map<Connector, Handler> handlers = Maps.newLinkedHashMap();

        for (Connector connector : appConnectors) {
            server.addConnector(connector);
            handlers.put(connector, applicationHandler);
        }

        for (Connector connector : adConnectors) {
            server.addConnector(connector);
            handlers.put(connector, adminHandler);
        }

        return new RoutingHandler(handlers);
    }
}
