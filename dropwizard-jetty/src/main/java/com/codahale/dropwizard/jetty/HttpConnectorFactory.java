package com.codahale.dropwizard.jetty;

import com.codahale.dropwizard.util.Duration;
import com.codahale.dropwizard.util.Size;
import com.codahale.dropwizard.util.SizeUnit;
import com.codahale.dropwizard.validation.MinDuration;
import com.codahale.dropwizard.validation.MinSize;
import com.codahale.dropwizard.validation.PortRange;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.jetty9.InstrumentedConnectionFactory;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import org.eclipse.jetty.io.ArrayByteBufferPool;
import org.eclipse.jetty.io.ByteBufferPool;
import org.eclipse.jetty.server.*;
import org.eclipse.jetty.util.thread.ScheduledExecutorScheduler;
import org.eclipse.jetty.util.thread.Scheduler;
import org.eclipse.jetty.util.thread.ThreadPool;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;

@JsonTypeName("http")
public class HttpConnectorFactory implements ConnectorFactory {
    public static ConnectorFactory application() {
        final HttpConnectorFactory factory = new HttpConnectorFactory();
        factory.port = 8080;
        return factory;
    }

    public static ConnectorFactory admin() {
        final HttpConnectorFactory factory = new HttpConnectorFactory();
        factory.port = 8081;
        return factory;
    }

    @PortRange
    private int port = 8080;

    private String bindHost = null;

    @NotNull
    @MinSize(128)
    private Size headerCacheSize = Size.bytes(512);

    @NotNull
    @MinSize(value = 8, unit = SizeUnit.KILOBYTES)
    private Size outputBufferSize = Size.kilobytes(32);

    @NotNull
    @MinSize(value = 1, unit = SizeUnit.KILOBYTES)
    private Size maxRequestHeaderSize = Size.kilobytes(8);

    @NotNull
    @MinSize(value = 1, unit = SizeUnit.KILOBYTES)
    private Size maxResponseHeaderSize = Size.kilobytes(8);

    @NotNull
    @MinSize(value = 1, unit = SizeUnit.KILOBYTES)
    private Size inputBufferSize = Size.kilobytes(8);

    @NotNull
    @MinDuration(value = 1, unit = TimeUnit.MILLISECONDS)
    private Duration idleTimeout = Duration.seconds(30);

    @NotNull
    @MinSize(value = 1, unit = SizeUnit.BYTES)
    private Size minBufferPoolSize = Size.bytes(64);

    @NotNull
    @MinSize(value = 1, unit = SizeUnit.BYTES)
    private Size bufferPoolIncrement = Size.bytes(1024);

    @NotNull
    @MinSize(value = 1, unit = SizeUnit.BYTES)
    private Size maxBufferPoolSize = Size.kilobytes(64);

    @Min(1)
    private int acceptorThreads = Math.max(1, Runtime.getRuntime().availableProcessors() / 2);

    @Min(1)
    private int selectorThreads = Runtime.getRuntime().availableProcessors();

    @Min(-1)
    private int acceptQueueSize = -1;

    private boolean reuseAddress = true;
    private Duration soLingerTime = null;
    private boolean useServerHeader = false;
    private boolean useDateHeader = true;
    private boolean useForwardedHeaders = true;

    @JsonProperty
    public int getPort() {
        return port;
    }

    @JsonProperty
    public void setPort(int port) {
        this.port = port;
    }

    @JsonProperty
    public String getBindHost() {
        return bindHost;
    }

    @JsonProperty
    public void setBindHost(String bindHost) {
        this.bindHost = bindHost;
    }

    @JsonProperty
    public Size getHeaderCacheSize() {
        return headerCacheSize;
    }

    @JsonProperty
    public void setHeaderCacheSize(Size headerCacheSize) {
        this.headerCacheSize = headerCacheSize;
    }

    @JsonProperty
    public Size getOutputBufferSize() {
        return outputBufferSize;
    }

    @JsonProperty
    public void setOutputBufferSize(Size outputBufferSize) {
        this.outputBufferSize = outputBufferSize;
    }

    @JsonProperty
    public Size getMaxRequestHeaderSize() {
        return maxRequestHeaderSize;
    }

    @JsonProperty
    public void setMaxRequestHeaderSize(Size maxRequestHeaderSize) {
        this.maxRequestHeaderSize = maxRequestHeaderSize;
    }

    @JsonProperty
    public Size getMaxResponseHeaderSize() {
        return maxResponseHeaderSize;
    }

    @JsonProperty
    public void setMaxResponseHeaderSize(Size maxResponseHeaderSize) {
        this.maxResponseHeaderSize = maxResponseHeaderSize;
    }

    @JsonProperty
    public Size getInputBufferSize() {
        return inputBufferSize;
    }

    @JsonProperty
    public void setInputBufferSize(Size inputBufferSize) {
        this.inputBufferSize = inputBufferSize;
    }

    @JsonProperty
    public Duration getIdleTimeout() {
        return idleTimeout;
    }

    @JsonProperty
    public void setIdleTimeout(Duration idleTimeout) {
        this.idleTimeout = idleTimeout;
    }

    @JsonProperty
    public Size getMinBufferPoolSize() {
        return minBufferPoolSize;
    }

    @JsonProperty
    public void setMinBufferPoolSize(Size minBufferPoolSize) {
        this.minBufferPoolSize = minBufferPoolSize;
    }

    @JsonProperty
    public Size getBufferPoolIncrement() {
        return bufferPoolIncrement;
    }

    @JsonProperty
    public void setBufferPoolIncrement(Size bufferPoolIncrement) {
        this.bufferPoolIncrement = bufferPoolIncrement;
    }

    @JsonProperty
    public Size getMaxBufferPoolSize() {
        return maxBufferPoolSize;
    }

    @JsonProperty
    public void setMaxBufferPoolSize(Size maxBufferPoolSize) {
        this.maxBufferPoolSize = maxBufferPoolSize;
    }

    @JsonProperty
    public int getAcceptorThreads() {
        return acceptorThreads;
    }

    @JsonProperty
    public void setAcceptorThreads(int acceptorThreads) {
        this.acceptorThreads = acceptorThreads;
    }

    @JsonProperty
    public int getSelectorThreads() {
        return selectorThreads;
    }

    @JsonProperty
    public void setSelectorThreads(int selectorThreads) {
        this.selectorThreads = selectorThreads;
    }

    @JsonProperty
    public int getAcceptQueueSize() {
        return acceptQueueSize;
    }

    @JsonProperty
    public void setAcceptQueueSize(int acceptQueueSize) {
        this.acceptQueueSize = acceptQueueSize;
    }

    @JsonProperty
    public boolean isReuseAddress() {
        return reuseAddress;
    }

    @JsonProperty
    public void setReuseAddress(boolean reuseAddress) {
        this.reuseAddress = reuseAddress;
    }

    @JsonProperty
    public Duration getSoLingerTime() {
        return soLingerTime;
    }

    @JsonProperty
    public void setSoLingerTime(Duration soLingerTime) {
        this.soLingerTime = soLingerTime;
    }

    @JsonProperty
    public boolean isUseServerHeader() {
        return useServerHeader;
    }

    @JsonProperty
    public void setUseServerHeader(boolean useServerHeader) {
        this.useServerHeader = useServerHeader;
    }

    @JsonProperty
    public boolean isUseDateHeader() {
        return useDateHeader;
    }

    @JsonProperty
    public void setUseDateHeader(boolean useDateHeader) {
        this.useDateHeader = useDateHeader;
    }

    @JsonProperty
    public boolean isUseForwardedHeaders() {
        return useForwardedHeaders;
    }

    @JsonProperty
    public void setUseForwardedHeaders(boolean useForwardedHeaders) {
        this.useForwardedHeaders = useForwardedHeaders;
    }

    @Override
    public Connector build(Server server,
                           MetricRegistry metrics,
                           String name,
                           ThreadPool threadPool) {
        final HttpConfiguration httpConfig = buildHttpConfiguration();

        final HttpConnectionFactory httpConnectionFactory = buildHttpConnectionFactory(httpConfig);

        final Scheduler scheduler = new ScheduledExecutorScheduler();

        final ByteBufferPool bufferPool = buildBufferPool();

        final String timerName = name(HttpConnectionFactory.class,
                                      bindHost,
                                      Integer.toString(port),
                                      "connections");
        return buildConnector(server, scheduler, bufferPool, name, threadPool,
                              new InstrumentedConnectionFactory(httpConnectionFactory,
                                                                metrics.timer(timerName)));
    }

    protected ServerConnector buildConnector(Server server,
                                             Scheduler scheduler,
                                             ByteBufferPool bufferPool,
                                             String name,
                                             ThreadPool threadPool,
                                             ConnectionFactory... factories) {
        final ServerConnector connector = new ServerConnector(server,
                                                              threadPool,
                                                              scheduler,
                                                              bufferPool,
                                                              acceptorThreads,
                                                              selectorThreads,
                                                              factories);
        connector.setPort(port);
        connector.setHost(bindHost);
        connector.setAcceptQueueSize(acceptQueueSize);
        connector.setReuseAddress(reuseAddress);
        if (soLingerTime != null) {
            connector.setSoLingerTime((int) soLingerTime.toSeconds());
        }
        connector.setIdleTimeout(idleTimeout.toMilliseconds());
        connector.setName(name);

        return connector;
    }

    protected HttpConnectionFactory buildHttpConnectionFactory(HttpConfiguration httpConfig) {
        final HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory(httpConfig);
        httpConnectionFactory.setInputBufferSize((int) inputBufferSize.toBytes());
        return httpConnectionFactory;
    }

    protected HttpConfiguration buildHttpConfiguration() {
        final HttpConfiguration httpConfig = new HttpConfiguration();
        httpConfig.setHeaderCacheSize((int) headerCacheSize.toBytes());
        httpConfig.setOutputBufferSize((int) outputBufferSize.toBytes());
        httpConfig.setRequestHeaderSize((int) maxRequestHeaderSize.toBytes());
        httpConfig.setResponseHeaderSize((int) maxResponseHeaderSize.toBytes());
        httpConfig.setSendDateHeader(useDateHeader);
        httpConfig.setSendServerVersion(useServerHeader);

        if (useForwardedHeaders) {
            httpConfig.addCustomizer(new ForwardedRequestCustomizer());
        }
        return httpConfig;
    }

    protected ByteBufferPool buildBufferPool() {
        return new ArrayByteBufferPool((int) minBufferPoolSize.toBytes(),
                                       (int) bufferPoolIncrement.toBytes(),
                                       (int) maxBufferPoolSize.toBytes());
    }
}
