package io.dropwizard.jetty;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.dropwizard.metrics.jetty11.InstrumentedConnectionFactory;
import io.dropwizard.util.DataSize;
import io.dropwizard.util.DataSizeUnit;
import io.dropwizard.util.Duration;
import io.dropwizard.validation.MinDataSize;
import io.dropwizard.validation.MinDuration;
import io.dropwizard.validation.PortRange;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.valueextraction.Unwrapping;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.jetty.http.CookieCompliance;
import org.eclipse.jetty.http.HttpCompliance;
import org.eclipse.jetty.http.UriCompliance;
import org.eclipse.jetty.io.ArrayByteBufferPool;
import org.eclipse.jetty.io.ByteBufferPool;
import org.eclipse.jetty.server.ConnectionFactory;
import org.eclipse.jetty.server.Connector;
import org.eclipse.jetty.server.ForwardedRequestCustomizer;
import org.eclipse.jetty.server.HttpConfiguration;
import org.eclipse.jetty.server.HttpConnectionFactory;
import org.eclipse.jetty.server.ProxyConnectionFactory;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.util.ArrayUtil;
import org.eclipse.jetty.util.thread.ScheduledExecutorScheduler;
import org.eclipse.jetty.util.thread.Scheduler;
import org.eclipse.jetty.util.thread.ThreadPool;

import java.util.Objects;
import java.util.Optional;
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
 *             The maximum allowed size in bytes for the HTTP request line and HTTP request headers.
 *             Larger headers will allow for more and/or larger cookies plus larger form content
 *             encoded in a URL. However, larger headers consume more memory and can make a server
 *             more vulnerable to denial of service attacks.
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
 *         <td>(Jetty's default)</td>
 *         <td>The number of worker threads dedicated to accepting connections.
 *         By default is <i>max</i>(1, <i>min</i>(4, #CPUs/8)).</td>
 *     </tr>
 *     <tr>
 *         <td>{@code selectorThreads}</td>
 *         <td>(Jetty's default)</td>
 *         <td>The number of worker threads dedicated to sending and receiving data.
 *         By default is <i>max</i>(1, <i>min</i>(4, #CPUs/2)).</td>
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
 *         <td>{@code minResponseDataPerSecond}</td>
 *         <td>0 bytes</td>
 *         <td>
 *             The minimum response data rate in bytes per second; or &lt;=0 for no limit
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code minRequestDataPerSecond}</td>
 *         <td>0 bytes</td>
 *         <td>
 *             The minimum request data rate in bytes per second; or &lt;=0 for no limit
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code useForwardedHeaders}</td>
 *         <td>false</td>
 *         <td>
 *             Whether or not to look at {@code X-Forwarded-*} headers added by proxies. See
 *             {@link ForwardedRequestCustomizer} for details.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code useProxyProtocol}</td>
 *         <td>false</td>
 *         <td>
 *             Enable jetty proxy protocol header support.
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code httpCompliance}</td>
 *         <td>RFC7230</td>
 *         <td>
 *             This sets the http compliance level used by Jetty when parsing http, this can be useful when using a
 *             non-RFC7230 compliant front end, such as nginx, which can produce multi-line headers when forwarding
 *             client certificates using proxy_set_header X-SSL-CERT $ssl_client_cert;
 *
 *             Possible values are set forth in the org.eclipse.jetty.http.HttpCompliance enum:
 *             <ul>
 *                 <li>RFC7230: Disallow header folding.</li>
 *                 <li>RFC2616: Allow header folding.</li>
 *             </ul>
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code uriCompliance}</td>
 *         <td>DEFAULT</td>
 *         <td>
 *             This sets the uri compliance level used by Jetty when parsing http, this can be useful when
 *             attempting to avoid breaking changes with Jetty 10 and onward;
 *
 *             Possible values are set forth in the org.eclipse.jetty.http.UriCompliance enum and include:
 *             <ul>
 *                 <li>DEFAULT: The default compliance mode that extends RFC3986 compliance with additional violations to avoid most ambiguous URIs.</li>
 *                 <li>LEGACY: Compliance mode that models Jetty-9.4 behavior.</li>
 *                 <li>RFC3986: Compliance mode that exactly follows RFC3986, including allowing all additional ambiguous URI Violations.</li>
 *             </ul>
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code requestCookieCompliance}</td>
 *         <td>RFC6265</td>
 *         <td>
 *             This sets the cookie compliance level used by Jetty when parsing request {@code Cookie} headers,
 *             this can be useful when needing to support Version=1 cookies defined in RFC2109 (and continued in
 *             RFC2965) which allows for special/reserved characters (control, separator, et al) to be enclosed within
 *             double quotes when used in a cookie value;
 *
 *             Possible values are set forth in the org.eclipse.jetty.http.CookieCompliance enum:
 *             <ul>
 *                 <li>RFC6265: Special characters in cookie values must be encoded.</li>
 *                 <li>RFC2965: Allows for special characters enclosed within double quotes.</li>
 *             </ul>
 *         </td>
 *     </tr>
 *     <tr>
 *         <td>{@code responseCookieCompliance}</td>
 *         <td>RFC6265</td>
 *         <td>
 *             This sets the cookie compliance level used by Jetty when generating response {@code Set-Cookie} headers,
 *             this can be useful when needing to support Version=1 cookies defined in RFC2109 (and continued in
 *             RFC2965) which allows for special/reserved characters (control, separator, et al) to be enclosed within
 *             double quotes when used in a cookie value;
 *
 *             Possible values are set forth in the org.eclipse.jetty.http.CookieCompliance enum:
 *             <ul>
 *                 <li>RFC6265: Special characters in cookie values must be encoded.</li>
 *                 <li>RFC2965: Allows for special characters enclosed within double quotes.</li>
 *             </ul>
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

    @Nullable
    private String bindHost;

    private boolean inheritChannel = false;

    @NotNull
    @MinDataSize(128)
    private DataSize headerCacheSize = DataSize.bytes(512);

    @NotNull
    @MinDataSize(value = 8, unit = DataSizeUnit.KIBIBYTES)
    private DataSize outputBufferSize = DataSize.kibibytes(32);

    @NotNull
    @MinDataSize(value = 1, unit = DataSizeUnit.KIBIBYTES)
    private DataSize maxRequestHeaderSize = DataSize.kibibytes(8);

    @NotNull
    @MinDataSize(value = 1, unit = DataSizeUnit.KIBIBYTES)
    private DataSize maxResponseHeaderSize = DataSize.kibibytes(8);

    @NotNull
    @MinDataSize(value = 1, unit = DataSizeUnit.KIBIBYTES)
    private DataSize inputBufferSize = DataSize.kibibytes(8);

    @NotNull
    @MinDuration(value = 1, unit = TimeUnit.MILLISECONDS)
    private Duration idleTimeout = Duration.seconds(30);

    @NotNull
    @MinDataSize(0)
    private DataSize minResponseDataPerSecond = DataSize.bytes(0);

    @NotNull
    @MinDataSize(0)
    private DataSize minRequestDataPerSecond = DataSize.bytes(0);

    @NotNull
    @MinDataSize(value = 1, unit = DataSizeUnit.BYTES)
    private DataSize minBufferPoolSize = DataSize.bytes(64);

    @NotNull
    @MinDataSize(value = 1, unit = DataSizeUnit.BYTES)
    private DataSize bufferPoolIncrement = DataSize.bytes(1024);

    @NotNull
    @MinDataSize(value = 1, unit = DataSizeUnit.BYTES)
    private DataSize maxBufferPoolSize = DataSize.kibibytes(64);

    @Min(value = 1, payload = Unwrapping.Unwrap.class)
    private Optional<Integer> acceptorThreads = Optional.empty();

    @Min(value = 1, payload = Unwrapping.Unwrap.class)
    private Optional<Integer> selectorThreads = Optional.empty();

    @Min(0)
    @Nullable
    private Integer acceptQueueSize;

    private boolean reuseAddress = true;

    private boolean useServerHeader = false;
    private boolean useDateHeader = true;
    private boolean useForwardedHeaders = false;
    private boolean useProxyProtocol = false;
    @JsonSerialize(using = HttpComplianceSerializer.class)
    @JsonDeserialize(using = HttpComplianceDeserializer.class)
    private HttpCompliance httpCompliance = HttpCompliance.RFC7230;
    @JsonSerialize(using = UriComplianceSerializer.class)
    @JsonDeserialize(using = UriComplianceDeserializer.class)
    private UriCompliance uriCompliance = UriCompliance.DEFAULT;
    @JsonSerialize(using = CookieComplianceSerializer.class)
    @JsonDeserialize(using = CookieComplianceDeserializer.class)
    private CookieCompliance requestCookieCompliance = CookieCompliance.RFC6265;
    @JsonSerialize(using = CookieComplianceSerializer.class)
    @JsonDeserialize(using = CookieComplianceDeserializer.class)
    private CookieCompliance responseCookieCompliance = CookieCompliance.RFC6265;

    @JsonProperty
    public int getPort() {
        return port;
    }

    @JsonProperty
    public void setPort(int port) {
        this.port = port;
    }

    @JsonProperty
    @Nullable
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
    public DataSize getHeaderCacheSize() {
        return headerCacheSize;
    }

    @JsonProperty
    public void setHeaderCacheSize(DataSize headerCacheSize) {
        this.headerCacheSize = headerCacheSize;
    }

    @JsonProperty
    public DataSize getOutputBufferSize() {
        return outputBufferSize;
    }

    @JsonProperty
    public void setOutputBufferSize(DataSize outputBufferSize) {
        this.outputBufferSize = outputBufferSize;
    }

    @JsonProperty
    public DataSize getMaxRequestHeaderSize() {
        return maxRequestHeaderSize;
    }

    @JsonProperty
    public void setMaxRequestHeaderSize(DataSize maxRequestHeaderSize) {
        this.maxRequestHeaderSize = maxRequestHeaderSize;
    }

    @JsonProperty
    public DataSize getMaxResponseHeaderSize() {
        return maxResponseHeaderSize;
    }

    @JsonProperty
    public void setMaxResponseHeaderSize(DataSize maxResponseHeaderSize) {
        this.maxResponseHeaderSize = maxResponseHeaderSize;
    }

    @JsonProperty
    public DataSize getInputBufferSize() {
        return inputBufferSize;
    }

    @JsonProperty
    public void setInputBufferSize(DataSize inputBufferSize) {
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
    public DataSize getMinBufferPoolSize() {
        return minBufferPoolSize;
    }

    @JsonProperty
    public void setMinBufferPoolSize(DataSize minBufferPoolSize) {
        this.minBufferPoolSize = minBufferPoolSize;
    }

    @JsonProperty
    public DataSize getBufferPoolIncrement() {
        return bufferPoolIncrement;
    }

    @JsonProperty
    public void setBufferPoolIncrement(DataSize bufferPoolIncrement) {
        this.bufferPoolIncrement = bufferPoolIncrement;
    }

    @JsonProperty
    public DataSize getMaxBufferPoolSize() {
        return maxBufferPoolSize;
    }

    @JsonProperty
    public void setMaxBufferPoolSize(DataSize maxBufferPoolSize) {
        this.maxBufferPoolSize = maxBufferPoolSize;
    }

    @JsonProperty
    public DataSize getMinResponseDataPerSecond() {
        return minResponseDataPerSecond;
    }

    @JsonProperty
    public void setMinResponseDataPerSecond(DataSize minResponseDataPerSecond) {
        this.minResponseDataPerSecond = minResponseDataPerSecond;
    }

    @JsonProperty
    public DataSize getMinRequestDataPerSecond() {
        return minRequestDataPerSecond;
    }

    @JsonProperty
    public void setMinRequestDataPerSecond(DataSize minRequestDataPerSecond) {
        this.minRequestDataPerSecond = minRequestDataPerSecond;
    }

    @JsonProperty
    public Optional<Integer> getAcceptorThreads() {
        return acceptorThreads;
    }

    @JsonProperty
    public void setAcceptorThreads(Optional<Integer> acceptorThreads) {
        this.acceptorThreads = acceptorThreads;
    }

    @JsonProperty
    public Optional<Integer> getSelectorThreads() {
        return selectorThreads;
    }

    @JsonProperty
    public void setSelectorThreads(Optional<Integer> selectorThreads) {
        this.selectorThreads = selectorThreads;
    }

    @JsonProperty
    @Nullable
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

    /**
     * @since 2.0
     */
    @JsonProperty
    public boolean isUseProxyProtocol() {
        return useProxyProtocol;
    }

    /**
     * @since 2.0
     */
    @JsonProperty
    public void setUseProxyProtocol(boolean useProxyProtocol) {
        this.useProxyProtocol = useProxyProtocol;
    }

    @JsonProperty
    public HttpCompliance getHttpCompliance() {
        return httpCompliance;
    }

    @JsonProperty
    public void setHttpCompliance(HttpCompliance httpCompliance) {
        this.httpCompliance = httpCompliance;
    }

    @JsonProperty
    public UriCompliance getUriCompliance() {
        return uriCompliance;
    }

    @JsonProperty
    public void setUriCompliance(UriCompliance uriCompliance) {
        this.uriCompliance = uriCompliance;
    }

    /**
     * @since 2.0
     */
    @JsonProperty
    public CookieCompliance getRequestCookieCompliance() {
        return requestCookieCompliance;
    }

    /**
     * @since 2.0
     */
    @JsonProperty
    public void setRequestCookieCompliance(CookieCompliance requestCookieCompliance) {
        this.requestCookieCompliance = requestCookieCompliance;
    }

    /**
     * @since 2.0
     */
    @JsonProperty
    public CookieCompliance getResponseCookieCompliance() {
        return responseCookieCompliance;
    }

    /**
     * @since 2.0
     */
    @JsonProperty
    public void setResponseCookieCompliance(CookieCompliance responseCookieCompliance) {
        this.responseCookieCompliance = responseCookieCompliance;
    }

    private static class HttpComplianceSerializer extends StringMethodSerializer<HttpCompliance> {
        public HttpComplianceSerializer() {
            super(HttpCompliance.class, HttpCompliance::getName);
        }
    }

    private static class HttpComplianceDeserializer extends StringMethodDeserializer<HttpCompliance> {
        public HttpComplianceDeserializer() {
            super(HttpCompliance.class, HttpCompliance::valueOf);
        }
    }

    private static class CookieComplianceSerializer extends StringMethodSerializer<CookieCompliance> {
        public CookieComplianceSerializer() {
            super(CookieCompliance.class, CookieCompliance::getName);
        }
    }

    private static class CookieComplianceDeserializer extends StringMethodDeserializer<CookieCompliance> {
        public CookieComplianceDeserializer() {
            super(CookieCompliance.class, CookieCompliance::valueOf);
        }
    }

    private static class UriComplianceSerializer extends StringMethodSerializer<UriCompliance> {
        public UriComplianceSerializer() {
            super(UriCompliance.class, UriCompliance::getName);
        }
    }

    private static class UriComplianceDeserializer extends StringMethodDeserializer<UriCompliance> {
        public UriComplianceDeserializer() {
            super(UriCompliance.class, UriCompliance::valueOf);
        }
    }

    @Override
    public Connector build(Server server,
                           MetricRegistry metrics,
                           String name,
                           @Nullable ThreadPool threadPool) {
        final HttpConfiguration httpConfig = buildHttpConfiguration();

        final HttpConnectionFactory httpConnectionFactory = buildHttpConnectionFactory(httpConfig);

        final Scheduler scheduler = new ScheduledExecutorScheduler();

        final ByteBufferPool bufferPool = buildBufferPool();

        return buildConnector(server, scheduler, bufferPool, name, threadPool,
                              new InstrumentedConnectionFactory(httpConnectionFactory,
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
                                             @Nullable ThreadPool threadPool,
                                             ConnectionFactory... factories) {
        if (useProxyProtocol) {
            factories = ArrayUtil.prependToArray(new ProxyConnectionFactory(), factories, ConnectorFactory.class);
        }

        final ServerConnector connector = new ServerConnector(server,
                                                              threadPool,
                                                              scheduler,
                                                              bufferPool,
                                                              acceptorThreads.orElse(-1),
                                                              selectorThreads.orElse(-1),
                                                              factories);
        connector.setPort(port);
        connector.setHost(bindHost);
        connector.setInheritChannel(inheritChannel);
        // if we do not set the acceptQueueSize, when jetty
        // creates the ServerSocket, it uses the default backlog of 50, and
        // not the value from the OS. Therefore we set to the value
        // obtained from NetUtil, which will attempt to read the value from the OS.
        // somaxconn setting
        int actualAcceptQueueSize = Objects.requireNonNullElseGet(acceptQueueSize, NetUtil::getTcpBacklog);
        connector.setAcceptQueueSize(actualAcceptQueueSize);

        connector.setReuseAddress(reuseAddress);
        connector.setIdleTimeout(idleTimeout.toMilliseconds());
        connector.setName(name);

        return connector;
    }

    protected HttpConnectionFactory buildHttpConnectionFactory(HttpConfiguration httpConfig) {
        HttpConfiguration clonedConfiguration = new HttpConfiguration(httpConfig);
        clonedConfiguration.setHttpCompliance(httpCompliance);
        clonedConfiguration.setUriCompliance(uriCompliance);
        final HttpConnectionFactory httpConnectionFactory = new HttpConnectionFactory(clonedConfiguration);
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
        httpConfig.setMinResponseDataRate(minResponseDataPerSecond.toBytes());
        httpConfig.setMinRequestDataRate(minRequestDataPerSecond.toBytes());
        httpConfig.setRequestCookieCompliance(requestCookieCompliance);
        httpConfig.setResponseCookieCompliance(responseCookieCompliance);

        if (useForwardedHeaders) {
            httpConfig.addCustomizer(new ForwardedRequestCustomizer());
        }

        return httpConfig;
    }

    protected ByteBufferPool buildBufferPool() {
        return buildBufferPool((int) minBufferPoolSize.toBytes(),
                               (int) bufferPoolIncrement.toBytes(),
                               (int) maxBufferPoolSize.toBytes());
    }

    // This method only exists so that mockito can spy on the constructor parameters.
    ByteBufferPool buildBufferPool(int minCapacity, int factor, int maxCapacity) {
        return new ArrayByteBufferPool(minCapacity, factor, maxCapacity);
    }
}
