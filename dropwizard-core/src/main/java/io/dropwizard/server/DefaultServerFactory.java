package io.dropwizard.server;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.base.MoreObjects;
import io.dropwizard.jetty.ConnectorFactory;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.jetty.RoutingHandler;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.component.ContainerLifeCycle;
import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

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
    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultServerFactory.class);

    @Valid
    @NotNull
    private List<ConnectorFactory> applicationConnectors = Collections.singletonList(HttpConnectorFactory.application());

    @Valid
    @NotNull
    private List<ConnectorFactory> adminConnectors = Collections.singletonList(HttpConnectorFactory.admin());

    @Min(2)
    private int adminMaxThreads = 64;

    @Min(1)
    private int adminMinThreads = 1;

    @NotEmpty
    private String applicationContextPath = "/";

    @NotEmpty
    private String adminContextPath = "/";

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

    @JsonProperty
    public String getApplicationContextPath() {
        return applicationContextPath;
    }

    @JsonProperty
    public void setApplicationContextPath(final String applicationContextPath) {
        this.applicationContextPath = applicationContextPath;
    }

    @JsonProperty
    public String getAdminContextPath() {
        return adminContextPath;
    }

    @JsonProperty
    public void setAdminContextPath(final String adminContextPath) {
        this.adminContextPath = adminContextPath;
    }

    @Override
    public Server build(Environment environment) {
    	// ensures that the environment is configured before the server is built
    	configure(environment);

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
        final Handler gzipHandler = buildGzipHandler(routingHandler);
        server.setHandler(addStatsHandler(addRequestLog(server, gzipHandler, environment.getName())));
        return server;
    }

    @Override
    public void configure(Environment environment) {
        LOGGER.info("Registering jersey handler with root path prefix: {}", applicationContextPath);
        environment.getApplicationContext().setContextPath(applicationContextPath);

        LOGGER.info("Registering admin handler with root path prefix: {}", adminContextPath);
        environment.getAdminContext().setContextPath(adminContextPath);
    }

    private RoutingHandler buildRoutingHandler(MetricRegistry metricRegistry,
                                               Server server,
                                               Handler applicationHandler,
                                               Handler adminHandler) {
        final List<Connector> appConnectors = buildAppConnectors(metricRegistry, server);

        final List<Connector> adConnectors = buildAdminConnectors(metricRegistry, server);

        final Map<Connector, Handler> handlers = new LinkedHashMap<>();

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
        // threadpool is shared between all the connectors, so it should be managed by the server instead of the
        // individual connectors
        final QueuedThreadPool threadPool = new QueuedThreadPool(adminMaxThreads, adminMinThreads);
        threadPool.setName("dw-admin");
        server.addBean(threadPool);

        final List<Connector> connectors = new ArrayList<>();
        for (ConnectorFactory factory : adminConnectors) {
            final Connector connector = factory.build(server, metricRegistry, "admin", threadPool);
            if (connector instanceof ContainerLifeCycle) {
                ((ContainerLifeCycle) connector).unmanage(threadPool);
            }
            connectors.add(connector);
        }
        return connectors;
    }

    private List<Connector> buildAppConnectors(MetricRegistry metricRegistry, Server server) {
        final List<Connector> connectors = new ArrayList<>();
        for (ConnectorFactory factory : applicationConnectors) {
            connectors.add(factory.build(server, metricRegistry, "application", null));
        }
        return connectors;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("applicationConnectors", applicationConnectors)
                .add("adminConnectors", adminConnectors)
                .add("adminMaxThreads", adminMaxThreads)
                .add("adminMinThreads", adminMinThreads)
                .add("applicationContextPath", applicationContextPath)
                .add("adminContextPath", adminContextPath)
                .toString();
    }
}
