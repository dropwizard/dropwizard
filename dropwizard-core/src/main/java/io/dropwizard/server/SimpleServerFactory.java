package io.dropwizard.server;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.collect.ImmutableMap;
import io.dropwizard.jetty.ConnectorFactory;
import io.dropwizard.jetty.ContextRoutingHandler;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.jetty.MutableServletContextHandler;
import io.dropwizard.setup.Environment;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
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

    private Optional<String> applicationContextPath = Optional.empty();

    private Optional<String> adminContextPath = Optional.empty();

    @JsonProperty
    public ConnectorFactory getConnector() {
        return connector;
    }

    @JsonProperty
    public void setConnector(ConnectorFactory factory) {
        this.connector = factory;
    }

    @JsonProperty
    public Optional<String> getApplicationContextPath() {
        return applicationContextPath;
    }

    @JsonProperty
    public void setApplicationContextPath(String contextPath) {
        this.applicationContextPath = Optional.ofNullable(contextPath);
    }

    @JsonProperty
    public Optional<String> getAdminContextPath() {
        return adminContextPath;
    }

    @JsonProperty
    public void setAdminContextPath(String contextPath) {
        this.adminContextPath = Optional.ofNullable(contextPath);
    }

    @Override
    public Server build(Environment environment) {
        printBanner(environment.getName());
        final ThreadPool threadPool = createThreadPool(environment.metrics());
        final Server server = buildServer(environment.lifecycle(), threadPool);
        final MutableServletContextHandler applicationContext = environment.getApplicationContext();
        final MutableServletContextHandler adminContext = environment.getAdminContext();

        LOGGER.info("Registering jersey handler with root path prefix: {}", applicationContextPath);
        if (applicationContextPath.isPresent()) {
        	applicationContext.setContextPath(applicationContextPath.get());
        }
        final Handler applicationHandler = createAppServlet(server,
                                                            environment.jersey(),
                                                            environment.getObjectMapper(),
                                                            environment.getValidator(),
                                                            environment.getApplicationContext(),
                                                            environment.getJerseyServletContainer(),
                                                            environment.metrics());

        LOGGER.info("Registering admin handler with root path prefix: {}", adminContextPath);
        if (adminContextPath.isPresent()) {
        	adminContext.setContextPath(adminContextPath.get());
        }
        final Handler adminHandler = createAdminServlet(server,
                                                        environment.getAdminContext(),
                                                        environment.metrics(),
                                                        environment.healthChecks());

        final Connector conn = connector.build(server,
                                               environment.metrics(),
                                               environment.getName(),
                                               null);

        server.addConnector(conn);

        if (applicationContext.getContextPath().equals("/") &&
        		applicationContext.getContextPath().equals(adminContext.getContextPath())) {
        	// contextRouting handler does not allow the same context path for both handlers
        	// if they are the same, use the default paths.
        	applicationContext.setContextPath("/application");
        	adminContext.setContextPath("/admin");
        }
        final ContextRoutingHandler routingHandler = new ContextRoutingHandler(ImmutableMap.of(
        		environment.getApplicationContext().getContextPath(), applicationHandler,
        		environment.getAdminContext().getContextPath(), adminHandler
        ));
        final Handler gzipHandler = buildGzipHandler(routingHandler);
        server.setHandler(addStatsHandler(addRequestLog(server, gzipHandler, environment.getName())));

        return server;
    }
}
