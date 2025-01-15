package io.dropwizard.unixsocket;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.metrics.jetty11.InstrumentedConnectionFactory;
import org.jspecify.annotations.Nullable;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.unixdomain.server.UnixDomainServerConnector;
import org.eclipse.jetty.util.thread.ScheduledExecutorScheduler;
import org.eclipse.jetty.util.thread.ThreadPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Builds Unix Domain Socket connectors.
 * <p/>
 * <b>Configuration Parameters:</b>
 * <table>
 *     <tr>
 *         <td>Name</td>
 *         <td>Default</td>
 *         <td>Description</td>
 *     </tr>
 *     <tr>
 *         <td>{@code path}</td>
 *         <td>/tmp/dropwizard.sock</td>
 *         <td>
 *             The path to the unix domain socket file.
 *         </td>
 *     </tr>
 * </table>
 * <p/>
 */
@JsonTypeName("unix-socket")
public class UnixSocketConnectorFactory extends HttpConnectorFactory {

    private static final Logger LOGGER = LoggerFactory.getLogger(UnixSocketConnectorFactory.class);

    private String path = "/tmp/dropwizard.sock";
    private boolean deleteSocketFileOnStartup;

    @JsonProperty
    public String getPath() {
        return path;
    }

    @JsonProperty
    public void setPath(String path) {
        this.path = path;
    }

    @JsonProperty
    public boolean isDeleteSocketFileOnStartup() {
        return deleteSocketFileOnStartup;
    }

    @JsonProperty
    public void setDeleteSocketFileOnStartup(boolean deleteSocketFileOnStartup) {
        this.deleteSocketFileOnStartup = deleteSocketFileOnStartup;
    }

    @Override
    public Connector build(Server server,
                           MetricRegistry metrics,
                           String name,
                           @Nullable ThreadPool threadPool) {
        var scheduler = new ScheduledExecutorScheduler();
        var bufferPool = buildBufferPool();
        var httpConfig = buildHttpConfiguration();
        var httpConnectionFactory = buildHttpConnectionFactory(httpConfig);
        var instrumentedConnectionFactory = new InstrumentedConnectionFactory(httpConnectionFactory,
            metrics.timer(httpConnections()));

        final UnixDomainServerConnector connector = new UnixDomainServerConnector(server,
            threadPool,
            scheduler,
            bufferPool,
            getAcceptorThreads().orElse(-1),
            getSelectorThreads().orElse(-1),
            instrumentedConnectionFactory);
        if (getAcceptQueueSize() != null) {
            connector.setAcceptQueueSize(getAcceptQueueSize());
        }

        var unixDomainPath = Paths.get(path);
        connector.setUnixDomainPath(unixDomainPath);
        connector.setIdleTimeout(getIdleTimeout().toMilliseconds());
        connector.setName(name);

        if (deleteSocketFileOnStartup) {
            // in case there is a leftover domain socket due to ungraceful stop, try to delete it first.
            try {
                Files.deleteIfExists(unixDomainPath);
            } catch (IOException e) {
                LOGGER.warn("Failed to delete existing unix domain socket file at {}.", path);
            }
        }
        return connector;
    }

    @Override
    protected String httpConnections() {
        return name(UnixSocketConnectorFactory.class, path, "connections");
    }
}
