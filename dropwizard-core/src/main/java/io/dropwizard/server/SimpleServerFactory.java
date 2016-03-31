package io.dropwizard.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.jetty.ConnectorFactory;
import io.dropwizard.jetty.ContextRoutingHandler;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.hibernate.validator.constraints.NotEmpty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

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
 *         <td>{@code applicationContextPath}</td>
 *         <td>{@code /application}</td>
 *         <td>The context path of the application servlets, including Jersey.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code adminContextPath}</td>
 *         <td>{@code /admin}</td>
 *         <td>The context path of the admin servlets, including metrics and tasks.</td>
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

    private static final Logger LOGGER = LoggerFactory.getLogger(SimpleServerFactory.class);

    @Valid
    @NotNull
    private ConnectorFactory connector = HttpConnectorFactory.application();

    @NotEmpty
    private String applicationContextPath = "/application";

    @NotEmpty
    private String adminContextPath = "/admin";

    @JsonProperty
    public ConnectorFactory getConnector() {
        return connector;
    }

    @JsonProperty
    public void setConnector(ConnectorFactory factory) {
        this.connector = factory;
    }

    @JsonProperty
    public String getApplicationContextPath() {
        return applicationContextPath;
    }

    @JsonProperty
    public void setApplicationContextPath(String contextPath) {
        this.applicationContextPath = contextPath;
    }

    @JsonProperty
    public String getAdminContextPath() {
        return adminContextPath;
    }

    @JsonProperty
    public void setAdminContextPath(String contextPath) {
        this.adminContextPath = contextPath;
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

        final Connector conn = connector.build(server,
                                               environment.metrics(),
                                               environment.getName(),
                                               null);

        server.addConnector(conn);

        final ContextRoutingHandler routingHandler = new ContextRoutingHandler(ImmutableMap.of(
                applicationContextPath, applicationHandler,
                adminContextPath, adminHandler
        ));
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
}
