package com.codahale.dropwizard.spdy;

import com.codahale.dropwizard.jetty.HttpsConnectorFactory;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jetty9.InstrumentedConnectionFactory;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.eclipse.jetty.io.ByteBufferPool;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.spdy.api.SPDY;
import org.eclipse.jetty.spdy.server.NPNServerConnectionFactory;
import org.eclipse.jetty.spdy.server.http.HTTPSPDYServerConnectionFactory;
import org.eclipse.jetty.spdy.server.http.PushStrategy;
import org.eclipse.jetty.util.ssl.SslContextFactory;
import org.eclipse.jetty.util.thread.ScheduledExecutorScheduler;
import org.eclipse.jetty.util.thread.Scheduler;
import org.eclipse.jetty.util.thread.ThreadPool;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import static com.codahale.metrics.MetricRegistry.name;

@JsonTypeName("spdy3")
public class Spdy3ConnectorFactory extends HttpsConnectorFactory {
    @Valid
    @NotNull
    private PushStrategyFactory pushStrategyFactory = new NonePushStrategyFactory();

    @JsonProperty("pushStrategy")
    public PushStrategyFactory getPushStrategyFactory() {
        return pushStrategyFactory;
    }

    @JsonProperty("pushStrategy")
    public void setPushStrategyFactory(PushStrategyFactory pushStrategyFactory) {
        this.pushStrategyFactory = pushStrategyFactory;
    }

    @Override
    public Connector build(Server server, MetricRegistry metrics, String name, ThreadPool threadPool) {
        logSupportedParameters();

        final HttpConfiguration httpConfig = buildHttpConfiguration();

        final HttpConnectionFactory httpConnectionFactory = buildHttpConnectionFactory(httpConfig);

        final SslContextFactory sslContextFactory = buildSslContextFactory();
        server.addBean(sslContextFactory);

        final PushStrategy pushStrategy = pushStrategyFactory.build();
        final HTTPSPDYServerConnectionFactory spdy3Factory =
                new HTTPSPDYServerConnectionFactory(SPDY.V3, httpConfig, pushStrategy);

        final NPNServerConnectionFactory npnFactory =
                new NPNServerConnectionFactory("spdy/3", "spdy/2", "http/1.1");
        npnFactory.setDefaultProtocol("http/1.1");

        final HTTPSPDYServerConnectionFactory spdy2Factory =
                new HTTPSPDYServerConnectionFactory(SPDY.V2, httpConfig, pushStrategy);

        final SslConnectionFactory sslConnectionFactory =
                new SslConnectionFactory(sslContextFactory, "npn");

        final Scheduler scheduler = new ScheduledExecutorScheduler();

        final ByteBufferPool bufferPool = buildBufferPool();

        final String timerName = name(HttpConnectionFactory.class, getBindHost(), Integer.toString(getPort()), "connections");

        return buildConnector(server, scheduler, bufferPool, name, threadPool,
                              new InstrumentedConnectionFactory(sslConnectionFactory, metrics.timer(timerName)),
                              npnFactory,
                              spdy3Factory,
                              spdy2Factory,
                              httpConnectionFactory);
    }
}
