package com.codahale.dropwizard.server;

import com.codahale.dropwizard.jetty.ConnectorFactory;
import com.codahale.dropwizard.jetty.HttpConnectorFactory;
import com.codahale.dropwizard.jetty.RoutingHandler;
import com.codahale.dropwizard.setup.Environment;
import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.Lists;
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
 *         <td>{@code applicationConnectors}</td>
 *         <td>An {@link HttpConnectorFactory HTTP connector} listening on port 8080.</td>
 *         <td>A set of {@link ConnectorFactory connectors} which will handle application requests.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code adminConnectors}</td>
 *         <td>An {@link HttpConnectorFactory HTTP connector} listening on port 8081.</td>
 *         <td>A set of {@link ConnectorFactory connectors} which will handle admin requests.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code adminMaxThreads}</td>
 *         <td>64</td>
 *         <td>The maximum number of threads to use for admin requests.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code adminMinThreads}</td>
 *         <td>1</td>
 *         <td>The minimum number of threads to use for admin requests.</td>
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
    private List<ConnectorFactory> applicationConnectors =
            Lists.newArrayList(HttpConnectorFactory.application());

    @Valid
    @NotNull
    private List<ConnectorFactory> adminConnectors =
            Lists.newArrayList(HttpConnectorFactory.admin());

    @Min(2)
    private int adminMaxThreads = 64;

    @Min(1)
    private int adminMinThreads = 1;

    @JsonProperty
    public List<ConnectorFactory> getApplicationConnectors() {
        return applicationConnectors;
    }

    @JsonProperty
    public void setApplicationConnectors(List<ConnectorFactory> connectors) {
        this.applicationConnectors = connectors;
    }

    @JsonProperty
    public List<ConnectorFactory> getAdminConnectors() {
        return adminConnectors;
    }

    @JsonProperty
    public void setAdminConnectors(List<ConnectorFactory> connectors) {
        this.adminConnectors = connectors;
    }

    @JsonProperty
    public int getAdminMaxThreads() {
        return adminMaxThreads;
    }

    @JsonProperty
    public void setAdminMaxThreads(int adminMaxThreads) {
        this.adminMaxThreads = adminMaxThreads;
    }

    @JsonProperty
    public int getAdminMinThreads() {
        return adminMinThreads;
    }

    @JsonProperty
    public void setAdminMinThreads(int adminMinThreads) {
        this.adminMinThreads = adminMinThreads;
    }

    @Override
    public Server build(Environment environment) {
        printBanner(environment.getName());
        final ThreadPool threadPool = createThreadPool(environment.metrics());
        final Server server = buildServer(environment.lifecycle(), threadPool);
        final Handler applicationHandler = createAppServlet(server,
                                                            environment.jersey(),
                                                            environment.getObjectMapper(),
                                                            environment.getValidator(),
                                                            environment.getApplicationContext(),
                                                            environment.getJerseyServletContainer(),
                                                            environment.metrics());
        final Handler adminHandler = createAdminServlet(server,
                                                        environment.getAdminContext(),
                                                        environment.metrics(),
                                                        environment.healthChecks());
        final RoutingHandler routingHandler = buildRoutingHandler(environment.metrics(),
                                                                  server,
                                                                  applicationHandler,
                                                                  adminHandler);
        server.setHandler(addRequestLog(routingHandler, environment.getName()));
        return server;
    }

    private RoutingHandler buildRoutingHandler(MetricRegistry metricRegistry,
                                               Server server,
                                               Handler applicationHandler,
                                               Handler adminHandler) {
        final List<Connector> appConnectors = buildAppConnectors(metricRegistry, server);

        final List<Connector> adConnectors = buildAdminConnectors(metricRegistry, server);

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

    private List<Connector> buildAdminConnectors(MetricRegistry metricRegistry, Server server) {
        final QueuedThreadPool threadPool = new QueuedThreadPool(adminMaxThreads, adminMinThreads);
        threadPool.setName("dw-admin");

        final List<Connector> connectors = Lists.newArrayList();
        for (ConnectorFactory factory : adminConnectors) {
            connectors.add(factory.build(server, metricRegistry, "admin", threadPool));
        }
        return connectors;
    }

    private List<Connector> buildAppConnectors(MetricRegistry metricRegistry, Server server) {
        final List<Connector> connectors = Lists.newArrayList();
        for (ConnectorFactory factory : applicationConnectors) {
            connectors.add(factory.build(server, metricRegistry, "application", server.getThreadPool()));
        }
        return connectors;
    }
}
