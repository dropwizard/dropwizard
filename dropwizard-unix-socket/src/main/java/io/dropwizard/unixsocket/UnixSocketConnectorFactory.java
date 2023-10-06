package io.dropwizard.unixsocket;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.jetty.HttpConnectorFactory;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.jetty.io.ByteBufferPool;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.unixdomain.server.UnixDomainServerConnector;
import org.eclipse.jetty.util.thread.Scheduler;
import org.eclipse.jetty.util.thread.ThreadPool;

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

    private String path = "/tmp/dropwizard.sock";

    @JsonProperty
    public String getPath() {
        return path;
    }

    @JsonProperty
    public void setPath(String path) {
        this.path = path;
    }

    @Override
    protected Connector buildConnector(Server server, Scheduler scheduler, ByteBufferPool bufferPool, String name, @Nullable ThreadPool threadPool, ConnectionFactory... factories) {
        final UnixDomainServerConnector connector = new UnixDomainServerConnector(server,
            threadPool,
            scheduler,
            bufferPool,
            getAcceptorThreads().orElse(-1),
            getSelectorThreads().orElse(-1),
            factories);

        if (getAcceptQueueSize() != null) {
            connector.setAcceptQueueSize(getAcceptQueueSize());
        }
        connector.setUnixDomainPath(Paths.get(path));
        connector.setIdleTimeout(getIdleTimeout().toMilliseconds());
        connector.setName(name);
        return connector;
    }

    @Override
    protected String httpConnections() {
        return name(UnixSocketConnectorFactory.class, path, "connections");
    }
}
