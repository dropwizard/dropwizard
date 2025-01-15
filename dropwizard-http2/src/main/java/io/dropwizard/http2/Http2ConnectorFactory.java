package io.dropwizard.http2;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.jetty.HttpsConnectorFactory;
import io.dropwizard.jetty.SslReload;
import io.dropwizard.metrics.jetty11.InstrumentedConnectionFactory;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.jspecify.annotations.Nullable;
import org.eclipse.jetty.alpn.server.ALPNServerConnectionFactory;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.NegotiatingServerConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.SslConnectionFactory;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.ScheduledExecutorScheduler;
import org.eclipse.jetty.util.thread.ThreadPool;

import java.util.Arrays;
import java.util.Collections;

/**
 * Builds HTTP/2 over TLS (h2) connectors.
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
 *         <td>1024</td>
 *         <td>
 *             The maximum number of concurrently open streams allowed on a single HTTP/2 connection.
 *             Larger values increase parallelism, but cost a memory commitment.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code initialStreamRecvWindow}</td>
 *         <td>65535</td>
 *         <td>
 *             The initial flow control window size for a new stream. Larger values may allow greater throughput,
 *             but also risk head of line blocking if TCP/IP flow control is triggered.
 *         </td>
 *     </tr>
 * </table>
 * <p/>
 * For more configuration parameters, see {@link HttpsConnectorFactory}.
 *
 * @see HttpsConnectorFactory
 */
@JsonTypeName("h2")
public class Http2ConnectorFactory extends HttpsConnectorFactory {
    private static final String HTTP2_DEFAULT_CIPHER = "TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256";

    @Min(100)
    @Max(Integer.MAX_VALUE)
    private int maxConcurrentStreams = 1024;

    @Min(1)
    @Max(Integer.MAX_VALUE)
    private int initialStreamRecvWindow = 65535;

    @JsonProperty
    public int getMaxConcurrentStreams() {
        return maxConcurrentStreams;
    }

    @JsonProperty
    public void setMaxConcurrentStreams(int maxConcurrentStreams) {
        this.maxConcurrentStreams = maxConcurrentStreams;
    }

    @JsonProperty
    public int getInitialStreamRecvWindow() {
        return initialStreamRecvWindow;
    }

    @JsonProperty
    public void setInitialStreamRecvWindow(int initialStreamRecvWindow) {
        this.initialStreamRecvWindow = initialStreamRecvWindow;
    }

    @Override
    public Connector build(Server server, MetricRegistry metrics, String name, @Nullable ThreadPool threadPool) {
        // HTTP/2 requires that a server MUST support TLSv1.2 or higher and TLS_ECDHE_RSA_WITH_AES_128_GCM_SHA256 cipher
        // See https://datatracker.ietf.org/doc/html/rfc7540#section-9.2
        setSupportedProtocols(Arrays.asList("TLSv1.3", "TLSv1.2"));
        checkSupportedCipherSuites();

        // Setup connection factories
        final HttpConfiguration httpConfig = buildHttpConfiguration();
        final HttpConnectionFactory http1 = buildHttpConnectionFactory(httpConfig);
        final HTTP2ServerConnectionFactory http2 = new HTTP2ServerConnectionFactory(httpConfig);
        http2.setMaxConcurrentStreams(maxConcurrentStreams);
        http2.setInitialStreamRecvWindow(initialStreamRecvWindow);

        final NegotiatingServerConnectionFactory alpn = new ALPNServerConnectionFactory();
        alpn.setDefaultProtocol("http/1.1"); // Speak HTTP 1.1 over TLS if negotiation fails

        final SslContextFactory.Server sslContextFactory = configureSslContextFactory(new SslContextFactory.Server());
        sslContextFactory.addEventListener(logSslParameters(sslContextFactory));
        server.addBean(sslContextFactory);
        server.addBean(new SslReload(sslContextFactory, this::configureSslContextFactory));

        // We should use ALPN as a negotiation protocol. Old clients that don't support it will be served
        // via HTTPS. New clients, however, that want to use HTTP/2 will use TLS with ALPN extension.
        // If negotiation succeeds, the client and server switch to HTTP/2 protocol.
        final SslConnectionFactory sslConnectionFactory = new SslConnectionFactory(sslContextFactory, "alpn");

        return buildConnector(server, new ScheduledExecutorScheduler(), buildBufferPool(), name, threadPool,
            new InstrumentedConnectionFactory(sslConnectionFactory, metrics.timer(httpConnections())),
            alpn, http2, http1);
    }

    void checkSupportedCipherSuites() {
        if (getSupportedCipherSuites() == null) {
            setSupportedCipherSuites(Collections.singletonList(HTTP2_DEFAULT_CIPHER));
        } else if (!getSupportedCipherSuites().contains(HTTP2_DEFAULT_CIPHER)) {
            throw new IllegalArgumentException("HTTP/2 server configuration must include cipher: " + HTTP2_DEFAULT_CIPHER);
        }
    }
}
