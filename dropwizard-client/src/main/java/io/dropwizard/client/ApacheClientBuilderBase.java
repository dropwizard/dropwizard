package io.dropwizard.client;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.httpclient.HttpClientMetricNameStrategies;
import com.codahale.metrics.httpclient.HttpClientMetricNameStrategy;
import com.codahale.metrics.httpclient.InstrumentedHttpClientConnectionManager;
import com.google.common.annotations.VisibleForTesting;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;

abstract class ApacheClientBuilderBase<T extends ApacheClientBuilderBase, C extends HttpClientConfiguration> {
    protected C configuration;
    protected CredentialsProvider credentialsProvider = null;
    protected final MetricRegistry metricRegistry;
    protected String name;
    protected Environment environment;
    protected HttpRequestRetryHandler httpRequestRetryHandler;
    protected HttpRoutePlanner routePlanner = null;
    protected HttpClientMetricNameStrategy metricNameStrategy = HttpClientMetricNameStrategies.METHOD_ONLY;
    protected DnsResolver resolver = new SystemDefaultDnsResolver();
    protected Registry<ConnectionSocketFactory> registry
            = RegistryBuilder.<ConnectionSocketFactory>create()
            .register("http", PlainConnectionSocketFactory.getSocketFactory())
            .register("https", SSLConnectionSocketFactory.getSocketFactory())
            .build();

    public ApacheClientBuilderBase(MetricRegistry metricRegistry, C configuration) {
        this.metricRegistry = metricRegistry;
        using(configuration);
    }

    public ApacheClientBuilderBase(Environment environment, C configuration) {
        this(environment.metrics(), configuration);
        using(environment);
    }

    @SuppressWarnings("unchecked")
    private T self() {
        return (T) this;
    }

    /**
     * Use the given name. This is used in the user agent.
     *
     * @param name a name to use in the user agent.
     * @return {@code this}
     */
    public T name(String name) {
        this.name = name;
        return self();
    }

    /**
     * Use the given {@link HttpClientConfiguration} instance.
     *
     * @param configuration a {@link HttpClientConfiguration} instance
     * @return {@code this}
     */
    public T using(C configuration) {
        this.configuration = configuration;
        return self();
    }

    /**
     * Use the given {@link DnsResolver} instance.
     *
     * @param resolver a {@link DnsResolver} instance
     * @return {@code this}
     */
    public T using(DnsResolver resolver) {
        this.resolver = resolver;
        return self();
    }

    /**
     * Use the given {@link CredentialsProvider} instance.
     *
     * @param credentialsProvider a {@link CredentialsProvider} instance
     * @return {@code this}
     */
    public T using(CredentialsProvider credentialsProvider) {
        this.credentialsProvider = credentialsProvider;
        return self();
    }

    /**
     * Uses the given {@link Environment}.
     *
     * @param environment a Dropwizard {@link Environment}
     * @return {@code this}
     */
    public T using(Environment environment) {
        this.environment = environment;
        name(environment.getName());
        return self();
    }

    /**
     * Uses the {@link HttpRequestRetryHandler} for handling request retries.
     *
     * @param httpRequestRetryHandler an httpRequestRetryHandler
     * @return {@code this}
     */
    public T using(HttpRequestRetryHandler httpRequestRetryHandler) {
        this.httpRequestRetryHandler = httpRequestRetryHandler;
        return self();
    }

    /**
     * Use the given {@link Registry} instance.
     *
     * @param registry
     * @return {@code this}
     */
    public T using(Registry<ConnectionSocketFactory> registry) {
        this.registry = registry;
        return self();
    }

    /**
     * Use the given {@link HttpClientMetricNameStrategy} instance.
     *
     * @param metricNameStrategy a {@link HttpClientMetricNameStrategy} instance
     * @return {@code this}
     */
    public T using(HttpClientMetricNameStrategy metricNameStrategy) {
        this.metricNameStrategy = metricNameStrategy;
        return self();
    }

    /**
     * Use the given {@link org.apache.http.conn.routing.HttpRoutePlanner} instance.
     *
     * @param routePlanner a {@link org.apache.http.conn.routing.HttpRoutePlanner} instance
     * @return {@code this}
     */
    public T using(HttpRoutePlanner routePlanner) {
        this.routePlanner = routePlanner;
        return self();
    }

    /**
     * Create a RequestConfig based on the HttpClientConfiguration.  It sets
     * timeouts and cookie handling.
     *
     * @return a RequestConfig instance
     */
    protected RequestConfig createRequestConfig() {
        final String cookiePolicy = configuration.isCookiesEnabled() ? CookieSpecs.BEST_MATCH : CookieSpecs.IGNORE_COOKIES;
        final Integer timeout = (int) configuration.getTimeout().toMilliseconds();
        final Integer connectionTimeout = (int) configuration.getConnectionTimeout().toMilliseconds();
        return RequestConfig.custom()
                .setCookieSpec(cookiePolicy)
                .setSocketTimeout(timeout)
                .setConnectTimeout(connectionTimeout)
                .setStaleConnectionCheckEnabled(false)
                .build();
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
