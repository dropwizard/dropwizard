package io.dropwizard.http2;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.jetty.HttpsConnectorFactory;
import io.dropwizard.jetty.Jetty93InstrumentedConnectionFactory;
import org.eclipse.jetty.http2.server.HTTP2CServerConnectionFactory;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.ScheduledExecutorScheduler;
import org.eclipse.jetty.util.thread.ThreadPool;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

/**
 * Builds HTTP/2 clear text (h2c) connectors.
 * <p/>
 * <b>Configuration Parameters:</b>
 * <table>
 *     <tr>
 *         <td>Name</td>
 *         <td>Default</td>
 *         <td>Description</td>
 *     </tr>
 *     <tr>
 *         <td>{@code maxConcurrentStreams}</td>
 *         <td><1024</td>
 *         <td>
 *             The maximum number of concurrently open streams allowed on a single HTTP/2 connection.
 *             Larger values increase parallelism, but cost a memory commitment.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code initialStreamSendWindow}</td>
 *         <td>65535</td>
 *         <td>
 *             The initial flow control window size for a new stream. Larger values may allow greater throughput,
 *             but also risk head of line blocking if TCP/IP flow control is triggered.
 *         </td>
 *     </tr>
 * </table>
 * <p/>
 * For more configuration parameters, see {@link HttpsConnectorFactory}.
 * @see HttpConnectorFactory
 */
@JsonTypeName("h2c")
public class Http2CConnectorFactory extends HttpConnectorFactory {

    @Min(100)
    @Max(Integer.MAX_VALUE)
    private int maxConcurrentStreams = 1024;

    @Min(1)
    @Max(Integer.MAX_VALUE)
    private int initialStreamSendWindow = 65535;

    @JsonProperty
    public int getMaxConcurrentStreams() {
        return maxConcurrentStreams;
    }

    @JsonProperty
    public void setMaxConcurrentStreams(int maxConcurrentStreams) {
        this.maxConcurrentStreams = maxConcurrentStreams;
    }

    @JsonProperty
    public int getInitialStreamSendWindow() {
        return initialStreamSendWindow;
    }

    @JsonProperty
    public void setInitialStreamSendWindow(int initialStreamSendWindow) {
        this.initialStreamSendWindow = initialStreamSendWindow;
    }

    @Override
    public Connector build(Server server, MetricRegistry metrics, String name, ThreadPool threadPool) {

        // Prepare connection factories for HTTP/2c
        final HttpConfiguration httpConfig = buildHttpConfiguration();
        final HttpConnectionFactory http11 = buildHttpConnectionFactory(httpConfig);
        final HTTP2ServerConnectionFactory http2c = new HTTP2CServerConnectionFactory(httpConfig);
        http2c.setMaxConcurrentStreams(maxConcurrentStreams);
        http2c.setInitialStreamSendWindow(initialStreamSendWindow);

        // The server connector should use HTTP/1.1 by default. It affords to the server to stay compatible
        // with old clients. New clients which want to use HTTP/2, however, will make an HTTP/1.1 OPTIONS
        // request with an Upgrade header with "h2c" value. The server supports HTTP/2 clear text connections,
        // so it will return the predefined HTTP/2 preamble and the client and the server will switch to the
        // new protocol.
        return buildConnector(server, new ScheduledExecutorScheduler(), buildBufferPool(), name, threadPool,
                new Jetty93InstrumentedConnectionFactory(http11, metrics.timer(httpConnections())), http2c);
    }
}
