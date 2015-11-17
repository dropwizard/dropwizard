package io.dropwizard.http2;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.jetty.HttpsConnectorFactory;
import io.dropwizard.jetty.Jetty93InstrumentedConnectionFactory;
import org.eclipse.jetty.http2.server.HTTP2CServerConnectionFactory;
import org.eclipse.jetty.http2.server.HTTP2ServerConnectionFactory;
import org.eclipse.jetty.io.ByteBufferPool;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.util.thread.ScheduledExecutorScheduler;
import org.eclipse.jetty.util.thread.Scheduler;
import org.eclipse.jetty.util.thread.ThreadPool;

/**
 * Builds HTTP/2 clear text (h2c) connectors.
 *
 * @see HttpConnectorFactory
 */
@JsonTypeName("http2c")
public class Http2CConnectorFactory extends HttpConnectorFactory {

    @Override
    public Connector build(Server server, MetricRegistry metrics, String name, ThreadPool threadPool) {

        // Prepare connection factories for HTTP/2c
        final HttpConfiguration httpConfig = buildHttpConfiguration();
        final HttpConnectionFactory http11 = buildHttpConnectionFactory(httpConfig);
        final HTTP2ServerConnectionFactory http2c = new HTTP2CServerConnectionFactory(httpConfig);

        // The server connector should use HTTP/1.1 by default. It affords to the server to stay compatible
        // with old clients. New clients which want to use HTTP/2, however, will make an HTTP/1.1 OPTIONS
        // request with an Upgrade header with "h2c" value. The server supports HTTP/2 clear text connections,
        // so it will return the predefined HTTP/2 preamble and the client and the server will switch to the
        // new protocol.
        return buildConnector(server, new ScheduledExecutorScheduler(), buildBufferPool(), name, threadPool,
                new Jetty93InstrumentedConnectionFactory(http11, metrics.timer(httpConnections())), http2c);
    }
}
