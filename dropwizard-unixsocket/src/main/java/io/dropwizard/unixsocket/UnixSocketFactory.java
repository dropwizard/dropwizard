package io.dropwizard.unixsocket;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.jetty.HttpConnectorFactory;
import org.eclipse.jetty.io.ByteBufferPool;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.unixsocket.UnixSocketConnector;
import org.eclipse.jetty.util.thread.Scheduler;
import org.eclipse.jetty.util.thread.ThreadPool;

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
 *         <td>{@code socketFilename}</td>
 *         <td></tmp/dropwizard.sock</td>
 *         <td>
 *             The filename of the opened Unix socket on the filesystem.
 *         </td>
 *     </tr>
 * </table>
 * <p/>
 */
@JsonTypeName("unixsocket")
public class UnixSocketFactory extends HttpConnectorFactory {

    private String socketFilename = "/tmp/dropwizard.sock";

    @JsonProperty
    public String getSocketFilename() {
        return socketFilename;
    }

    @JsonProperty
    public void setSocketFilename(String socketFilename) {
        this.socketFilename = socketFilename;
    }

    @Override
    protected Connector buildConnector(Server server, Scheduler scheduler, ByteBufferPool bufferPool, String name,
                                       ThreadPool threadPool, ConnectionFactory... factories) {
        final UnixSocketConnector connector = new UnixSocketConnector(server, threadPool, scheduler, bufferPool,
            getSelectorThreads().orElse(-1), factories);
        if (getAcceptQueueSize() != null) {
            connector.setAcceptQueueSize(getAcceptQueueSize());
        }
        connector.setReuseAddress(isReuseAddress());
        connector.setUnixSocket(socketFilename);
        connector.setIdleTimeout(getIdleTimeout().toMilliseconds());
        connector.setName(name);
        return connector;
    }

    @Override
    protected String httpConnections() {
        return name(UnixSocketFactory.class,  socketFilename, "connections");
    }
}
