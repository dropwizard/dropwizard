package io.dropwizard.client;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.httpclient.HttpClientMetricNameStrategies;
import com.codahale.metrics.httpclient.HttpClientMetricNameStrategy;
import com.codahale.metrics.httpclient.InstrumentedHttpClientConnectionManager;
import com.codahale.metrics.httpclient.InstrumentedHttpRequestExecutor;
import com.google.common.annotations.VisibleForTesting;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpResponse;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;

/**
 * A convenience class for building {@link HttpClient} instances.
 * <p>
 * Among other things,
 * <ul>
 * <li>Disables stale connection checks</li>
 * <li>Disables Nagle's algorithm</li>
 * <li>Disables cookie management by default</li>
 * </ul>
 * </p>
 */
public class HttpClientBuilder {
    private static final HttpRequestRetryHandler NO_RETRIES = new HttpRequestRetryHandler() {
        @Override
        public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
            return false;
        }
    };

    private final MetricRegistry metricRegistry;
    private String environmentName;
    private HttpClientConfiguration configuration = new HttpClientConfiguration();
    private DnsResolver resolver = new SystemDefaultDnsResolver();
    private HttpRequestRetryHandler httpRequestRetryHandler;
    private Registry<ConnectionSocketFactory> registry
            = RegistryBuilder.<ConnectionSocketFactory>create()
            .register("http", PlainConnectionSocketFactory.getSocketFactory())
            .register("https", SSLConnectionSocketFactory.getSocketFactory())
            .build();
    private CredentialsProvider credentialsProvider = null;
    private HttpClientMetricNameStrategy metricNameStrategy = HttpClientMetricNameStrategies.METHOD_ONLY;
    private HttpRoutePlanner routePlanner = null;

    public HttpClientBuilder(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    public HttpClientBuilder(Environment environment) {
        this(environment.metrics());
        name(environment.getName());
    }

    /**
     * Use the given environment name. This is used in the user agent.
     *
     * @param environmentName an environment name to use in the user agent.
     * @return {@code this}
     */
    public HttpClientBuilder name(String environmentName) {
        this.environmentName = environmentName;
        return this;
    }

    /**
     * Use the given {@link HttpClientConfiguration} instance.
     *
     * @param configuration a {@link HttpClientConfiguration} instance
     * @return {@code this}
     */
    public HttpClientBuilder using(HttpClientConfiguration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Use the given {@link DnsResolver} instance.
     *
     * @param resolver a {@link DnsResolver} instance
     * @return {@code this}
     */
    public HttpClientBuilder using(DnsResolver resolver) {
        this.resolver = resolver;
        return this;
    }

    /**
     * Uses the {@link HttpRequestRetryHandler} for handling request retries.
     *
     * @param httpRequestRetryHandler an httpRequestRetryHandler
     * @return {@code this}
     */
    public HttpClientBuilder using(HttpRequestRetryHandler httpRequestRetryHandler) {
        this.httpRequestRetryHandler = httpRequestRetryHandler;
        return this;
    }

    /**
     * Use the given {@link Registry} instance.
     *
     * @param registry
     * @return {@code this}
     */
    public HttpClientBuilder using(Registry<ConnectionSocketFactory> registry) {
        this.registry = registry;
        return this;
    }

    /**
     * Use the given {@link HttpRoutePlanner} instance.
     *
     * @param routePlanner    a {@link HttpRoutePlanner} instance
     * @return {@code this}
     */
    public HttpClientBuilder using(HttpRoutePlanner routePlanner) {
        this.routePlanner = routePlanner;
        return this;
    }

    /**
     * Use the given {@link CredentialsProvider} instance.
     *
     * @param credentialsProvider a {@link CredentialsProvider} instance
     * @return {@code this}
     */
    public HttpClientBuilder using(CredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
        return this;
    }

    /**
     * Use the given {@link HttpClientMetricNameStrategy} instance.
     *
     * @param metricNameStrategy    a {@link HttpClientMetricNameStrategy} instance
     * @return {@code this}
     */
    public HttpClientBuilder using(HttpClientMetricNameStrategy metricNameStrategy) {
        this.metricNameStrategy = metricNameStrategy;
        return this;
    }

    /**
     * Builds the {@link HttpClient}.
     *
     * @param name
     * @return an {@link CloseableHttpClient}
     */
    public CloseableHttpClient build(String name) {
        final InstrumentedHttpClientConnectionManager manager = createConnectionManager(registry, name);
        return createClient(org.apache.http.impl.client.HttpClientBuilder.create(), manager, name);
    }

    /**
     * Map the parameters in {@link HttpClientConfiguration} to configuration on a
     * {@link org.apache.http.impl.client.HttpClientBuilder} instance
     *
     * @param builder
     * @param manager
     * @param name
     * @return the configured {@link CloseableHttpClient}
     */
    @VisibleForTesting
    protected CloseableHttpClient createClient(
            final org.apache.http.impl.client.HttpClientBuilder builder,
            final InstrumentedHttpClientConnectionManager manager,
            final String name) {
        final String cookiePolicy = configuration.isCookiesEnabled() ? CookieSpecs.BEST_MATCH : CookieSpecs.IGNORE_COOKIES;
        final Integer timeout = (int) configuration.getTimeout().toMilliseconds();
        final Integer connectionTimeout = (int) configuration.getConnectionTimeout().toMilliseconds();
        final Integer connectionRequestTimeout = (int) configuration.getConnectionRequestTimeout().toMilliseconds();
        final long keepAlive = configuration.getKeepAlive().toMilliseconds();
        final ConnectionReuseStrategy reuseStrategy = keepAlive == 0
                ? new NoConnectionReuseStrategy()
                : new DefaultConnectionReuseStrategy();
        final HttpRequestRetryHandler retryHandler = configuration.getRetries() == 0
                ? NO_RETRIES
                : (httpRequestRetryHandler == null ? new DefaultHttpRequestRetryHandler(configuration.getRetries(),
                false) : httpRequestRetryHandler);

        final RequestConfig requestConfig
                = RequestConfig.custom().setCookieSpec(cookiePolicy)
                .setSocketTimeout(timeout)
                .setConnectTimeout(connectionTimeout)
                .setConnectionRequestTimeout(connectionRequestTimeout)
                .setStaleConnectionCheckEnabled(false)
                .build();
        final SocketConfig socketConfig = SocketConfig.custom()
                .setTcpNoDelay(true)
                .setSoTimeout(timeout)
                .build();

        builder.setRequestExecutor(new InstrumentedHttpRequestExecutor(metricRegistry, metricNameStrategy, name))
                .setConnectionManager(manager)
                .setDefaultRequestConfig(requestConfig)
                .setDefaultSocketConfig(socketConfig)
                .setConnectionReuseStrategy(reuseStrategy)
                .setRetryHandler(retryHandler)
                .setUserAgent(createUserAgent(name));

        if (keepAlive != 0) {
            // either keep alive based on response header Keep-Alive,
            // or if the server can keep a persistent connection (-1), then override based on client's configuration
            builder.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy() {
                @Override
                public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                    final long duration = super.getKeepAliveDuration(response, context);
                    return (duration == -1) ? keepAlive : duration;
                }
            });
        }

        if (credentialsProvider != null) {
            builder.setDefaultCredentialsProvider(credentialsProvider);
        }

        if (routePlanner != null) {
            builder.setRoutePlanner(routePlanner);
        }

        return builder.build();
    }

    /**
     * Create a user agent string using the configured user agent if defined, otherwise
     * using a combination of the environment name and this client name
     *
     * @param name the name of this client
     * @return the user agent string to be used by this client
     */
    protected String createUserAgent(String name) {
        final String defaultUserAgent = environmentName == null ? name : String.format("%s (%s)", environmentName, name);
        return configuration.getUserAgent().or(defaultUserAgent);
    }


    /**
     * Create a InstrumentedHttpClientConnectionManager based on the
     * HttpClientConfiguration. It sets the maximum connections per route and
     * the maximum total connections that the connection manager can create
     *
     * @param registry
     * @param name
     * @return a InstrumentedHttpClientConnectionManger instance
     */
    protected InstrumentedHttpClientConnectionManager createConnectionManager(Registry<ConnectionSocketFactory> registry,
                                                                              String name) {
        final Duration ttl = configuration.getTimeToLive();
        final InstrumentedHttpClientConnectionManager manager = new InstrumentedHttpClientConnectionManager(
                metricRegistry,
                registry,
                null, null,
                resolver,
                ttl.getQuantity(),
                ttl.getUnit(),
                name);
        return configureConnectionManager(manager);
    }

    @VisibleForTesting
    protected InstrumentedHttpClientConnectionManager configureConnectionManager(
            InstrumentedHttpClientConnectionManager connectionManager) {
        connectionManager.setDefaultMaxPerRoute(configuration.getMaxConnectionsPerRoute());
        connectionManager.setMaxTotal(configuration.getMaxConnections());
        return connectionManager;
    }
}
