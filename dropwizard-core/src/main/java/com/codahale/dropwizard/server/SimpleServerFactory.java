package com.codahale.dropwizard.server;

import com.codahale.dropwizard.jetty.*;
import com.codahale.dropwizard.setup.Environment;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.ImmutableMap;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.ThreadPool;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

// TODO: 5/15/13 <coda> -- add tests for SimpleServerFactory

/**
 * A single-connector implementation of {@link ServerFactory}, suitable for PaaS deployments
 * (e.g., Heroku) where applications are limited to a single, runtime-defined port. A startup script
 * can override the port via {@code -Ddw.server.connector.port=$PORT}.
 * <p/>
 * <b>Configuration Parameters:</b>
 * <table>
 *     <tr>
 *         <td>Name</td>
 *         <td>Default</td>
 *         <td>Description</td>
 *     </tr>
 *     <tr>
 *         <td>{@code connector}</td>
 *         <td>An {@link HttpConnectorFactory HTTP connector} listening on port {@code 8080}.</td>
 *         <td>The {@link ConnectorFactory connector} which will handle both application and admin requests.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code application}</td>
 *         <td>A {@link ContextHandlerFactory context handler} for /application.</td>
 *         <td>The context handler for the application.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code admin}</td>
 *         <td>A {@link ContextHandlerFactory context handler} for /admin.</td>
 *         <td>The context handler for the admin servlets, including metrics and tasks.</td>
 *     </tr>
 * </table>
 * <p/>
 * For more configuration parameters, see {@link AbstractServerFactory}.
 *
 * @see ServerFactory
 * @see AbstractServerFactory
 */
@JsonTypeName("simple")
public class SimpleServerFactory extends AbstractServerFactory {
    @Valid
    @NotNull
    private ConnectorFactory connector = HttpConnectorFactory.application();

    @Valid
    @NotNull
    private ContextHandlerFactory appHandlerFactory = ContextHandlerFactory.application();

    @Valid
    @NotNull
    private ContextHandlerFactory adminHandlerFactory = ContextHandlerFactory.admin();

    @JsonProperty
    public ConnectorFactory getConnector() {
        return connector;
    }

    @JsonProperty
    public void setConnector(ConnectorFactory factory) {
        this.connector = factory;
    }

    @Override
    @JsonProperty("application")
    public ContextHandlerFactory getApplicationHandlerFactory() {
        return appHandlerFactory;
    }

    @JsonProperty("application")
    public void setApplicationHandler(ContextHandlerFactory factory) {
        this.appHandlerFactory = factory;
    }

    @Override
    @JsonProperty("admin")
    public ContextHandlerFactory getAdminHandlerFactory() {
        return adminHandlerFactory;
    }

    @JsonProperty("admin")
    public void setAdminHandler(ContextHandlerFactory factory) {
        this.adminHandlerFactory = factory;
    }

    @Override
    public Server build(Environment environment) {
        printBanner(environment.getName());
        final ThreadPool threadPool = appHandlerFactory.buildThreadPool(environment.metrics(), "dw");
        final Server server = buildServer(environment.lifecycle(), threadPool);

        final Handler applicationHandler = appHandlerFactory.build(server,
                                                                   environment.getApplicationContext(),
                                                                   environment.metrics(),
                                                                   environment.getName());

        addJerseyServlet(environment.getApplicationContext(),
                         environment.getJerseyServletContainer(),
                         environment.jersey(),
                         environment.getObjectMapper(),
                         environment.getValidator());

        final Handler adminHandler = adminHandlerFactory.build(server,
                                                               environment.getAdminContext(),
                                                               environment.metrics(),
                                                               environment.getName());

        addAdminServlets(environment.getAdminContext(),
                         environment.metrics(),
                         environment.healthChecks());

        final Connector conn = connector.build(server,
                                               environment.metrics(),
                                               environment.getName(),
                                               server.getThreadPool());

        server.addConnector(conn);

        final ContextRoutingHandler routingHandler = new ContextRoutingHandler(ImmutableMap.of(
                appHandlerFactory.getContextPath(), applicationHandler,
                adminHandlerFactory.getContextPath(), adminHandler
        ));
        server.setHandler(routingHandler);

        return server;
    }
}
