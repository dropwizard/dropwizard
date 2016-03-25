package io.dropwizard.client;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.httpclient.HttpClientMetricNameStrategies;
import com.codahale.metrics.httpclient.HttpClientMetricNameStrategy;
import com.codahale.metrics.httpclient.InstrumentedHttpClientConnectionManager;
import com.codahale.metrics.httpclient.InstrumentedHttpRequestExecutor;
import com.google.common.annotations.VisibleForTesting;
import io.dropwizard.client.proxy.AuthConfiguration;
import io.dropwizard.client.proxy.NonProxyListProxyRoutePlanner;
import io.dropwizard.client.proxy.ProxyConfiguration;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.Header;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.RedirectStrategy;
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
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.protocol.HttpContext;

import java.util.List;

/**
 * A convenience class for building {@link HttpClient} instances.
 * <p>
 * Among other things,
 * <ul>
 * <li>Disables stale connection checks by default</li>
 * <li>Disables Nagle's algorithm</li>
 * <li>Disables cookie management by default</li>
 * </ul>
 * </p>
 */
public class HttpClientBuilder {
    private static final HttpRequestRetryHandler NO_RETRIES = (exception, executionCount, context) -> false;

    private final MetricRegistry metricRegistry;
    private String environmentName;
    private Environment environment;
    private HttpClientConfiguration configuration = new HttpClientConfiguration();
    private DnsResolver resolver = new SystemDefaultDnsResolver();
    private HttpRequestRetryHandler httpRequestRetryHandler;
    private Registry<ConnectionSocketFactory> registry;

    private CredentialsProvider credentialsProvider = null;
    private HttpClientMetricNameStrategy metricNameStrategy = HttpClientMetricNameStrategies.METHOD_ONLY;
    private HttpRoutePlanner routePlanner = null;
    private RedirectStrategy redirectStrategy;
    private boolean disableContentCompression;
    private List<? extends Header> defaultHeaders;

    public HttpClientBuilder(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    public HttpClientBuilder(Environment environment) {
        this(environment.metrics());
        name(environment.getName());
        this.environment = environment;
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
     * Use the given {@link org.apache.http.client.RedirectStrategy} instance.
     *
     * @param redirectStrategy    a {@link org.apache.http.client.RedirectStrategy} instance
     * @return {@code this}
     */
    public HttpClientBuilder using(RedirectStrategy redirectStrategy) {
        this.redirectStrategy = redirectStrategy;
        return this;
    }

    /**
     * Use the given default headers for each HTTP request
     *
     * @param defaultHeaders HTTP headers
     * @return {@code} this
     */
    public HttpClientBuilder using(List<? extends Header> defaultHeaders) {
        this.defaultHeaders = defaultHeaders;
        return this;
    }

    /**
     * Disable support of decompression of responses
     *
     * @param disableContentCompression {@code true}, if disabled
     * @return {@code this}
     */
    public HttpClientBuilder disableContentCompression(boolean disableContentCompression) {
        this.disableContentCompression = disableContentCompression;
        return this;
    }

    /**
     * Builds the {@link HttpClient}.
     *
     * @param name
     * @return an {@link CloseableHttpClient}
     */
    public CloseableHttpClient build(String name) {
        final CloseableHttpClient client = buildWithDefaultRequestConfiguration(name).getClient();
        // If the environment is present, we tie the client with the server lifecycle
        if (environment != null) {
            environment.lifecycle().manage(new Managed() {
                @Override
                public void start() throws Exception {
                }

                @Override
                public void stop() throws Exception {
                    client.close();
                }
            });
        }
        return client;
    }

    /**
     * For internal use only, used in {@link io.dropwizard.client.JerseyClientBuilder}
     * to create an instance of {@link io.dropwizard.client.DropwizardApacheConnector}
     *
     * @param name
     * @return an {@link io.dropwizard.client.ConfiguredCloseableHttpClient}
     */
    ConfiguredCloseableHttpClient buildWithDefaultRequestConfiguration(String name) {
        return createClient(org.apache.http.impl.client.HttpClientBuilder.create(),
                createConnectionManager(createConfiguredRegistry(), name), name);
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
    protected ConfiguredCloseableHttpClient createClient(
            final org.apache.http.impl.client.HttpClientBuilder builder,
            final InstrumentedHttpClientConnectionManager manager,
            final String name) {
        final String cookiePolicy = configuration.isCookiesEnabled() ? CookieSpecs.DEFAULT : CookieSpecs.IGNORE_COOKIES;
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

        // create a tunnel through a proxy host if it's specified in the config
        final ProxyConfiguration proxy = configuration.getProxyConfiguration();
        if (proxy != null) {
            final HttpHost httpHost = new HttpHost(proxy.getHost(), proxy.getPort(), proxy.getScheme());
            builder.setRoutePlanner(new NonProxyListProxyRoutePlanner(httpHost, proxy.getNonProxyHosts()));
            // if the proxy host requires authentication then add the host credentials to the credentials provider
            final AuthConfiguration auth = proxy.getAuth();
            if (auth != null) {
                if (credentialsProvider == null) {
                    credentialsProvider = new BasicCredentialsProvider();
                }
                credentialsProvider.setCredentials(new AuthScope(httpHost),
                        new UsernamePasswordCredentials(auth.getUsername(), auth.getPassword()));
            }
        }

        if (credentialsProvider != null) {
            builder.setDefaultCredentialsProvider(credentialsProvider);
        }

        if (routePlanner != null) {
            builder.setRoutePlanner(routePlanner);
        }

        if (disableContentCompression) {
            builder.disableContentCompression();
        }

        if (redirectStrategy != null) {
            builder.setRedirectStrategy(redirectStrategy);
        }

        if (defaultHeaders != null) {
            builder.setDefaultHeaders(defaultHeaders);
        }

        return new ConfiguredCloseableHttpClient(builder.build(), requestConfig);
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
        return configuration.getUserAgent().orElse(defaultUserAgent);
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

    private Registry<ConnectionSocketFactory> createConfiguredRegistry() {
        if (registry != null) {
            return registry;
        }

        final SSLConnectionSocketFactory sslConnectionSocketFactory;
        if (configuration.getTlsConfiguration() == null) {
            sslConnectionSocketFactory = SSLConnectionSocketFactory.getSocketFactory();
        } else {
            sslConnectionSocketFactory = new DropwizardSSLConnectionSocketFactory(configuration.getTlsConfiguration())
                    .getSocketFactory();
        }

        return RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslConnectionSocketFactory)
                .build();
    }


    @VisibleForTesting
    protected InstrumentedHttpClientConnectionManager configureConnectionManager(
            InstrumentedHttpClientConnectionManager connectionManager) {
        connectionManager.setDefaultMaxPerRoute(configuration.getMaxConnectionsPerRoute());
        connectionManager.setMaxTotal(configuration.getMaxConnections());
        connectionManager.setValidateAfterInactivity((int) configuration.getValidateAfterInactivityPeriod().toMilliseconds());
        return connectionManager;
    }
}
