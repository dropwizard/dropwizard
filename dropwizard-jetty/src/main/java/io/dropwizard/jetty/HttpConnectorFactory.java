package io.dropwizard.jetty;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import io.dropwizard.util.Duration;
import io.dropwizard.util.Size;
import io.dropwizard.util.SizeUnit;
import io.dropwizard.validation.MinDuration;
import io.dropwizard.validation.MinSize;
import io.dropwizard.validation.PortRange;
import org.eclipse.jetty.io.ArrayByteBufferPool;
import org.eclipse.jetty.io.ByteBufferPool;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.ForwardedRequestCustomizer;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.thread.ScheduledExecutorScheduler;
import org.eclipse.jetty.util.thread.Scheduler;
import org.eclipse.jetty.util.thread.ThreadPool;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.concurrent.TimeUnit;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * Builds HTTP connectors.
 *
 * <p/>
 * <b>Configuration Parameters:</b>
 * <table>
 *     <tr>
 *         <td>Name</td>
 *         <td>Default</td>
 *         <td>Description</td>
 *     </tr>
 *     <tr>
 *         <td>{@code port}</td>
 *         <td>8080</td>
 *         <td>The TCP/IP port on which to listen for incoming connections.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code bindHost}</td>
 *         <td>(none)</td>
 *         <td>The hostname to bind to.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code inheritChannel}</td>
 *         <td>false</td>
 *         <td>
 *             Whether this connector uses a channel inherited from the JVM.
 *             Use it with <a href="https://github.com/kazuho/p5-Server-Starter">Server::Starter</a>,
 *             to launch an instance of Jetty on demand.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code headerCacheSize}</td>
 *         <td>512 bytes</td>
 *         <td>The size of the header field cache.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code outputBufferSize}</td>
 *         <td>32KiB</td>
 *         <td>
 *             The size of the buffer into which response content is aggregated before being sent to
 *             the client.  A larger buffer can improve performance by allowing a content producer
 *             to run without blocking, however larger buffers consume more memory and may induce
 *             some latency before a client starts processing the content.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code maxRequestHeaderSize}</td>
 *         <td>8KiB</td>
 *         <td>
 *             The maximum size of a request header. Larger headers will allow for more and/or
 *             larger cookies plus larger form content encoded  in a URL. However, larger headers
 *             consume more memory and can make a server more vulnerable to denial of service
 *             attacks.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code maxResponseHeaderSize}</td>
 *         <td>8KiB</td>
 *         <td>
 *             The maximum size of a response header. Larger headers will allow for more and/or
 *             larger cookies and longer HTTP headers (eg for redirection).  However, larger headers
 *             will also consume more memory.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code inputBufferSize}</td>
 *         <td>8KiB</td>
 *         <td>The size of the per-connection input buffer.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code idleTimeout}</td>
 *         <td>30 seconds</td>
 *         <td>
 *             The maximum idle time for a connection, which roughly translates to the
 *             {@link java.net.Socket#setSoTimeout(int)} call, although with NIO implementations
 *             other mechanisms may be used to implement the timeout.
 *             <p/>
 *             The max idle time is applied:
 *             <ul>
 *                 <li>When waiting for a new message to be received on a connection</li>
 *                 <li>When waiting for a new message to be sent on a connection</li>
 *             </ul>
 *             <p/>
 *             This value is interpreted as the maximum time between some progress being made on the
 *             connection. So if a single byte is read or written, then the timeout is reset.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code minBufferPoolSize}</td>
 *         <td>64 bytes</td>
 *         <td>The minimum size of the buffer pool.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code bufferPoolIncrement}</td>
 *         <td>1KiB</td>
 *         <td>The increment by which the buffer pool should be increased.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code maxBufferPoolSize}</td>
 *         <td>64KiB</td>
 *         <td>The maximum size of the buffer pool.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code acceptorThreads}</td>
 *         <td>half the # of CPUs</td>
 *         <td>The number of worker threads dedicated to accepting connections.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code selectorThreads}</td>
 *         <td>the # of CPUs</td>
 *         <td>The number of worker threads dedicated to sending and receiving data.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code acceptQueueSize}</td>
 *         <td>(OS default)</td>
 *         <td>The size of the TCP/IP accept queue for the listening socket.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code reuseAddress}</td>
 *         <td>true</td>
 *         <td>Whether or not {@code SO_REUSEADDR} is enabled on the listening socket.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code soLingerTime}</td>
 *         <td>(disabled)</td>
 *         <td>Enable/disable {@code SO_LINGER} with the specified linger time.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code useServerHeader}</td>
 *         <td>false</td>
 *         <td>Whether or not to add the {@code Server} header to each response.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code useDateHeader}</td>
 *         <td>true</td>
 *         <td>Whether or not to add the {@code Date} header to each response.</td>
 *     </tr>
 *     <tr>
 *         <td>{@code useForwardedHeaders}</td>
 *         <td>true</td>
 *         <td>
 *             Whether or not to look at {@code X-Forwarded-*} headers added by proxies. See
 *             {@link ForwardedRequestCustomizer} for details.
 *         </td>
 *     </tr>
 * </table>
 */
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

    private boolean inheritChannel = false;

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

    @Min(0)
    private Integer acceptQueueSize;

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
    public boolean isInheritChannel() {
        return inheritChannel;
    }

    @JsonProperty
    public void setInheritChannel(boolean inheritChannel) {
        this.inheritChannel = inheritChannel;
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
    public Integer getAcceptQueueSize() {
        return acceptQueueSize;
    }

    @JsonProperty
    public void setAcceptQueueSize(Integer acceptQueueSize) {
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

        return buildConnector(server, scheduler, bufferPool, name, threadPool,
                              new Jetty93InstrumentedConnectionFactory(httpConnectionFactory,
                                                                metrics.timer(httpConnections())));
    }

    /**
     * Get name of the timer that tracks incoming HTTP connections
     */
    protected String httpConnections() {
        return name(HttpConnectionFactory.class,  bindHost, Integer.toString(port), "connections");
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
        connector.setInheritChannel(inheritChannel);
        if (acceptQueueSize != null) {
            connector.setAcceptQueueSize(acceptQueueSize);
        }
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
