package io.dropwizard.client;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.httpclient5.HttpClientMetricNameStrategies;
import com.codahale.metrics.httpclient5.HttpClientMetricNameStrategy;
import com.codahale.metrics.httpclient5.InstrumentedHttpClientConnectionManager;
import com.codahale.metrics.httpclient5.InstrumentedHttpRequestExecutor;
import io.dropwizard.client.proxy.AuthConfiguration;
import io.dropwizard.client.proxy.NonProxyListProxyRoutePlanner;
import io.dropwizard.client.proxy.ProxyConfiguration;
import io.dropwizard.client.ssl.TlsConfiguration;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.util.Duration;
import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.HttpRequestRetryStrategy;
import org.apache.hc.client5.http.SystemDefaultDnsResolver;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.client5.http.auth.CredentialsStore;
import org.apache.hc.client5.http.auth.NTCredentials;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.StandardCookieSpec;
import org.apache.hc.client5.http.impl.DefaultConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.auth.BasicCredentialsProvider;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.protocol.RedirectStrategy;
import org.apache.hc.client5.http.routing.HttpRoutePlanner;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.ConnectionReuseStrategy;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.impl.DefaultConnectionReuseStrategy;
import org.apache.hc.core5.http.impl.io.HttpRequestExecutor;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpProcessor;
import org.apache.hc.core5.util.TimeValue;
import org.jspecify.annotations.Nullable;

import javax.net.ssl.HostnameVerifier;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * A convenience class for building {@link org.apache.hc.client5.http.classic.HttpClient} instances.
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
    private static final HttpRequestRetryStrategy NO_RETRIES = new HttpRequestRetryStrategy() {
        @Override
        public boolean retryRequest(HttpRequest request, IOException exception, int execCount, HttpContext context) {
            return false;
        }
        @Override
        public boolean retryRequest(HttpResponse response, int execCount, HttpContext context) {
            return false;
        }
        @Override
        @Nullable
        public TimeValue getRetryInterval(HttpResponse response, int execCount, HttpContext context) {
            return null;
        }
    };

    private final MetricRegistry metricRegistry;

    @Nullable
    private String environmentName;

    @Nullable
    private Environment environment;
    private HttpClientConfiguration configuration = new HttpClientConfiguration();
    private DnsResolver resolver = new SystemDefaultDnsResolver();

    @Nullable
    private HostnameVerifier verifier;

    @Nullable
    private HttpRequestRetryStrategy httpRequestRetryStrategy;

    @Nullable
    private Registry<ConnectionSocketFactory> registry;

    @Nullable
    private CredentialsStore credentialsStore;

    private HttpClientMetricNameStrategy metricNameStrategy = HttpClientMetricNameStrategies.METHOD_ONLY;

    @Nullable
    private HttpRoutePlanner routePlanner;

    @Nullable
    private RedirectStrategy redirectStrategy;
    private boolean disableContentCompression;

    @Nullable
    private List<? extends Header> defaultHeaders;

    @Nullable
    private HttpProcessor httpProcessor;

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
     * Use the given {@link HostnameVerifier} instance.
     *
     * @param verifier a {@link HostnameVerifier} instance
     * @return {@code this}
     */
    public HttpClientBuilder using(HostnameVerifier verifier) {
        this.verifier = verifier;
        return this;
    }

    /**
     * Uses the {@link HttpRequestRetryStrategy} for handling request retries.
     *
     * @param httpRequestRetryStrategy an {@link HttpRequestRetryStrategy}
     * @return {@code this}
     */
    public HttpClientBuilder using(HttpRequestRetryStrategy httpRequestRetryStrategy) {
        this.httpRequestRetryStrategy = httpRequestRetryStrategy;
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
     * Use the given {@link CredentialsStore} instance.
     *
     * @param credentialsStore a {@link CredentialsStore} instance
     * @return {@code this}
     */
    public HttpClientBuilder using(CredentialsStore credentialsStore) {
        this.credentialsStore = credentialsStore;
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
     * Use the given {@link RedirectStrategy} instance.
     *
     * @param redirectStrategy    a {@link RedirectStrategy} instance
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
     * Use the given {@link HttpProcessor} instance
     *
     * @param httpProcessor a {@link HttpProcessor} instance
     * @return {@code} this
     */
    public HttpClientBuilder using(HttpProcessor httpProcessor) {
        this.httpProcessor = httpProcessor;
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
     * Builds the {@link org.apache.hc.client5.http.classic.HttpClient}.
     *
     * @param name
     * @return an {@link org.apache.hc.client5.http.impl.classic.CloseableHttpClient}
     */
    public CloseableHttpClient build(String name) {
        final CloseableHttpClient client = buildWithDefaultRequestConfiguration(name).getClient();
        // If the environment is present, we tie the client with the server lifecycle
        if (environment != null) {
            environment.lifecycle().manage(new Managed() {
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
        return createClient(createBuilder(),
                createConnectionManager(createConfiguredRegistry(), name), name);
    }

    /**
     * Creates a {@link org.apache.hc.core5.http.impl.io.HttpRequestExecutor}.
     *
     * Intended for use by subclasses to provide a customized request executor.
     * The default implementation is an {@link com.codahale.metrics.httpclient5.InstrumentedHttpRequestExecutor}
     *
     * @param name
     * @return a {@link org.apache.hc.core5.http.impl.io.HttpRequestExecutor}
     * @since 2.0
     */
    protected HttpRequestExecutor createRequestExecutor(String name) {
        return new InstrumentedHttpRequestExecutor(metricRegistry, metricNameStrategy, name);
    }

    /**
     * Creates an Apache {@link org.apache.hc.client5.http.impl.classic.HttpClientBuilder}.
     *
     * Intended for use by subclasses to create builder instance from subclass of
     * {@link org.apache.hc.client5.http.impl.classic.HttpClientBuilder}
     *
     * @return an {@link HttpClientBuilder}
     * @since 2.0
     */
    protected org.apache.hc.client5.http.impl.classic.HttpClientBuilder createBuilder() {
        return org.apache.hc.client5.http.impl.classic.HttpClientBuilder.create();
    }

    /**
     * Configures an Apache {@link org.apache.hc.client5.http.impl.classic.HttpClientBuilder}.
     *
     * Intended for use by subclasses to inject HttpClientBuilder
     * configuration. The default implementation is an identity
     * function.
     */
    protected org.apache.hc.client5.http.impl.classic.HttpClientBuilder customizeBuilder(
        org.apache.hc.client5.http.impl.classic.HttpClientBuilder builder
    ) {
        return builder;
    }

    /**
     * Map the parameters in {@link HttpClientConfiguration} to configuration on a
     * {@link org.apache.hc.client5.http.impl.classic.HttpClientBuilder} instance
     *
     * @param builder
     * @param manager
     * @param name
     * @return the configured {@link CloseableHttpClient}
     */
    protected ConfiguredCloseableHttpClient createClient(
            final org.apache.hc.client5.http.impl.classic.HttpClientBuilder builder,
            final InstrumentedHttpClientConnectionManager manager,
            final String name) {
        final String cookiePolicy = configuration.isCookiesEnabled() ? StandardCookieSpec.RELAXED : StandardCookieSpec.IGNORE;
        final int timeout = (int) configuration.getTimeout().toMilliseconds();
        final int connectionTimeout = (int) configuration.getConnectionTimeout().toMilliseconds();
        final int connectionRequestTimeout = (int) configuration.getConnectionRequestTimeout().toMilliseconds();
        final long keepAlive = configuration.getKeepAlive().toMilliseconds();
        final ConnectionReuseStrategy reuseStrategy = keepAlive == 0
                ? ((request, response, context) -> false)
                : new DefaultConnectionReuseStrategy();
        final HttpRequestRetryStrategy retryHandler = configuration.getRetries() == 0
                ? NO_RETRIES
                : (httpRequestRetryStrategy == null ? new DefaultHttpRequestRetryStrategy(configuration.getRetries(),
                TimeValue.ofSeconds(1L)) : httpRequestRetryStrategy);
        final boolean protocolUpgradeEnabled = configuration.isProtocolUpgradeEnabled();

        final RequestConfig requestConfig
                = RequestConfig.custom().setCookieSpec(cookiePolicy)
                .setResponseTimeout(timeout, TimeUnit.MILLISECONDS)
                .setConnectTimeout(connectionTimeout, TimeUnit.MILLISECONDS)
                .setConnectionKeepAlive(TimeValue.of(-1, TimeUnit.MILLISECONDS))
                .setConnectionRequestTimeout(connectionRequestTimeout, TimeUnit.MILLISECONDS)
                .setProtocolUpgradeEnabled(protocolUpgradeEnabled)
                .build();
        final SocketConfig socketConfig = SocketConfig.custom()
                .setTcpNoDelay(true)
                .setSoTimeout(timeout, TimeUnit.MILLISECONDS)
                .build();

        manager.setDefaultSocketConfig(socketConfig);

        builder.setRequestExecutor(createRequestExecutor(name))
            .setConnectionManager(manager)
            .setDefaultRequestConfig(requestConfig)
            .setConnectionReuseStrategy(reuseStrategy)
            .setRetryStrategy(retryHandler)
            .setUserAgent(createUserAgent(name));

        if (keepAlive != 0) {
            // either keep alive based on response header Keep-Alive,
            // or if the server can keep a persistent connection (-1), then override based on client's configuration
            builder.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy() {
                @Override
                public TimeValue getKeepAliveDuration(HttpResponse response, HttpContext context) {
                    final TimeValue duration = super.getKeepAliveDuration(response, context);
                    return (duration.getDuration() == -1) ? TimeValue.ofMilliseconds(keepAlive) : duration;
                }
            });
        }

        // create a tunnel through a proxy host if it's specified in the config
        final ProxyConfiguration proxy = configuration.getProxyConfiguration();
        if (proxy != null) {
            final HttpHost httpHost = new HttpHost(proxy.getScheme(), proxy.getHost(), proxy.getPort());
            builder.setRoutePlanner(new NonProxyListProxyRoutePlanner(httpHost, proxy.getNonProxyHosts()));
            // if the proxy host requires authentication then add the host credentials to the credentials provider
            final AuthConfiguration auth = proxy.getAuth();
            if (auth != null) {
                if (credentialsStore == null) {
                    credentialsStore = new BasicCredentialsProvider();
                }
                // set the AuthScope
                AuthScope authScope = new AuthScope(httpHost, auth.getRealm(), auth.getAuthScheme());

                // set the credentials type
                Credentials credentials = configureCredentials(auth);

                credentialsStore.setCredentials(authScope, credentials);
            }
        }

        if (credentialsStore != null) {
            builder.setDefaultCredentialsProvider(credentialsStore);
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

        if (httpProcessor != null) {
            builder.addRequestInterceptorFirst(httpProcessor);
            builder.addResponseInterceptorLast(httpProcessor);
        }

        customizeBuilder(builder);

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
        final InstrumentedHttpClientConnectionManager manager = InstrumentedHttpClientConnectionManager.builder(metricRegistry)
            .socketFactoryRegistry(registry)
            .dnsResolver(resolver)
            .timeToLive(TimeValue.of(ttl.getQuantity(), ttl.getUnit()))
            .name(name)
            .build();
        return configureConnectionManager(manager);
    }

    Registry<ConnectionSocketFactory> createConfiguredRegistry() {
        if (registry != null) {
            return registry;
        }

        TlsConfiguration tlsConfiguration = configuration.getTlsConfiguration();
        if (tlsConfiguration == null && verifier != null) {
            tlsConfiguration = new TlsConfiguration();
        }

        final SSLConnectionSocketFactory sslConnectionSocketFactory;
        if (tlsConfiguration == null) {
            sslConnectionSocketFactory = SSLConnectionSocketFactory.getSocketFactory();
        } else {
            sslConnectionSocketFactory = new DropwizardSSLConnectionSocketFactory(tlsConfiguration,
                verifier).getSocketFactory();
        }

        return RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", sslConnectionSocketFactory)
                .build();
    }


    protected InstrumentedHttpClientConnectionManager configureConnectionManager(
            InstrumentedHttpClientConnectionManager connectionManager) {
        connectionManager.setDefaultMaxPerRoute(configuration.getMaxConnectionsPerRoute());
        connectionManager.setMaxTotal(configuration.getMaxConnections());
        connectionManager.setValidateAfterInactivity(TimeValue.ofMilliseconds((int) configuration.getValidateAfterInactivityPeriod().toMilliseconds()));
        return connectionManager;
    }

    /**
     * determine the Credentials implementation to use
     * @param auth
     * @return a {@code Credentials} instance, either {{@link UsernamePasswordCredentials} or {@link NTCredentials}}
     */
    protected Credentials configureCredentials(AuthConfiguration auth) {

        if (null != auth.getCredentialType() && auth.getCredentialType().equalsIgnoreCase(AuthConfiguration.NT_CREDS)) {
            return new NTCredentials(auth.getUsername(), auth.getPassword().toCharArray(), auth.getHostname(), auth.getDomain());
        } else {
            return new UsernamePasswordCredentials(auth.getUsername(), auth.getPassword().toCharArray());
        }

    }
}
