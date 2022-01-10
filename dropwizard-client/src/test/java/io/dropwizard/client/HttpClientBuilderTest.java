package io.dropwizard.client;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.httpclient.HttpClientMetricNameStrategies;
import com.codahale.metrics.httpclient.InstrumentedHttpClientConnectionManager;
import com.codahale.metrics.httpclient.InstrumentedHttpRequestExecutor;
import io.dropwizard.client.proxy.AuthConfiguration;
import io.dropwizard.client.proxy.ProxyConfiguration;
import io.dropwizard.client.ssl.TlsConfiguration;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.NTCredentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.RedirectStrategy;
import org.apache.http.client.ServiceUnavailableRetryStrategy;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpUriRequest;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.routing.HttpRoutePlanner;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.conn.DefaultRoutePlanner;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicListHeaderIterator;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestExecutor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.annotation.Nullable;
import javax.net.ssl.HostnameVerifier;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AnotherHttpClientBuilder extends org.apache.http.impl.client.HttpClientBuilder {
    public static AnotherHttpClientBuilder create() {
        return new AnotherHttpClientBuilder();
    }
}

class HttpClientBuilderTest {
    static class CustomRequestExecutor extends HttpRequestExecutor {
    }

    static class CustomBuilder extends HttpClientBuilder {
        public boolean customized;
        public org.apache.http.impl.client.HttpClientBuilder builder;

        public CustomBuilder(MetricRegistry metricRegistry) {
            this(metricRegistry, org.apache.http.impl.client.HttpClientBuilder.create());
        }

        public CustomBuilder(MetricRegistry metricRegistry, org.apache.http.impl.client.HttpClientBuilder builder) {
            super(metricRegistry);
            customized = false;
            this.builder = builder;
        }

        @Override
        protected org.apache.http.impl.client.HttpClientBuilder createBuilder() {
            return builder;
        }

        @Override
        protected HttpRequestExecutor createRequestExecutor(String name) {
            return new CustomRequestExecutor();
        }

        @Override
        protected org.apache.http.impl.client.HttpClientBuilder customizeBuilder(
            org.apache.http.impl.client.HttpClientBuilder builder
        ) {
            customized = true;
            return builder;
        }
    }

    private final Class<?> httpClientBuilderClass;
    private final Class<?> httpClientClass;
    private final Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
            .register("http", PlainConnectionSocketFactory.getSocketFactory())
            .register("https", SSLConnectionSocketFactory.getSocketFactory())
            .build();
    private HttpClientConfiguration configuration;
    private HttpClientBuilder builder;
    private InstrumentedHttpClientConnectionManager connectionManager;
    private org.apache.http.impl.client.HttpClientBuilder apacheBuilder;
    private AnotherHttpClientBuilder anotherApacheBuilder;

    public HttpClientBuilderTest() throws ClassNotFoundException {
        this.httpClientBuilderClass = Class.forName("org.apache.http.impl.client.HttpClientBuilder");
        this.httpClientClass = Class.forName("org.apache.http.impl.client.InternalHttpClient");
    }

    @BeforeEach
    void setUp() {
        final MetricRegistry metricRegistry = new MetricRegistry();
        configuration = new HttpClientConfiguration();
        builder = new HttpClientBuilder(metricRegistry);
        connectionManager = spy(InstrumentedHttpClientConnectionManager.builder(metricRegistry).socketFactoryRegistry(registry).build());
        apacheBuilder = org.apache.http.impl.client.HttpClientBuilder.create();
        anotherApacheBuilder = spy(AnotherHttpClientBuilder.create());
    }

    @AfterEach
    void validate() {
        validateMockitoUsage();
    }

    @Test
    void setsTheMaximumConnectionPoolSize() throws Exception {
        configuration.setMaxConnections(412);
        final ConfiguredCloseableHttpClient client = builder.using(configuration)
                .createClient(apacheBuilder, builder.configureConnectionManager(connectionManager), "test");

        assertThat(client).isNotNull();
        assertThat(spyHttpClientBuilderField("connManager", apacheBuilder)).isSameAs(connectionManager);
        verify(connectionManager).setMaxTotal(412);
    }


    @Test
    void setsTheMaximumRoutePoolSize() throws Exception {
        configuration.setMaxConnectionsPerRoute(413);
        final ConfiguredCloseableHttpClient client = builder.using(configuration)
                .createClient(apacheBuilder, builder.configureConnectionManager(connectionManager), "test");

        assertThat(client).isNotNull();
        assertThat(spyHttpClientBuilderField("connManager", apacheBuilder)).isSameAs(connectionManager);
        verify(connectionManager).setDefaultMaxPerRoute(413);
    }

    @Test
    void setsTheUserAgent() throws Exception {
        configuration.setUserAgent(Optional.of("qwerty"));
        assertThat(builder.using(configuration).createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        assertThat(spyHttpClientBuilderField("userAgent", apacheBuilder)).isEqualTo("qwerty");
    }

    @Test
    void canUseACustomDnsResolver() throws Exception {
        final DnsResolver resolver = mock(DnsResolver.class);
        final InstrumentedHttpClientConnectionManager manager =
                builder.using(resolver).createConnectionManager(registry, "test");

        // Yes, this is gross. Thanks, Apache!
        final Field connectionOperatorField =
                getInaccessibleField(PoolingHttpClientConnectionManager.class, "connectionOperator");
        final Object connectOperator = connectionOperatorField.get(manager);
        final Field dnsResolverField = getInaccessibleField(connectOperator.getClass(), "dnsResolver");
        assertThat(dnsResolverField.get(connectOperator)).isEqualTo(resolver);
    }


    @Test
    void usesASystemDnsResolverByDefault() throws Exception {
        final InstrumentedHttpClientConnectionManager manager = builder.createConnectionManager(registry, "test");

        // Yes, this is gross. Thanks, Apache!
        final Field connectionOperatorField =
                getInaccessibleField(PoolingHttpClientConnectionManager.class, "connectionOperator");
        final Object connectOperator = connectionOperatorField.get(manager);
        final Field dnsResolverField = getInaccessibleField(connectOperator.getClass(), "dnsResolver");
        assertThat(dnsResolverField.get(connectOperator)).isInstanceOf(SystemDefaultDnsResolver.class);
    }

    @Test
    void canUseACustomHostnameVerifierWhenTlsConfigurationNotSpecified() throws Exception {
        final HostnameVerifier customVerifier = (s, sslSession) -> false;

        final Registry<ConnectionSocketFactory> configuredRegistry;
        configuredRegistry = builder.using(customVerifier).createConfiguredRegistry();
        assertThat(configuredRegistry).isNotNull();

        final SSLConnectionSocketFactory socketFactory =
                (SSLConnectionSocketFactory) configuredRegistry.lookup("https");
        assertThat(socketFactory).isNotNull();

        final Field hostnameVerifierField =
                getInaccessibleField(SSLConnectionSocketFactory.class, "hostnameVerifier");
        assertThat(hostnameVerifierField.get(socketFactory)).isSameAs(customVerifier);
    }

    @Test
    void canUseACustomHostnameVerifierWhenTlsConfigurationSpecified() throws Exception {
        final TlsConfiguration tlsConfiguration = new TlsConfiguration();
        tlsConfiguration.setVerifyHostname(true);
        configuration.setTlsConfiguration(tlsConfiguration);

        final HostnameVerifier customVerifier = (s, sslSession) -> false;

        final Registry<ConnectionSocketFactory> configuredRegistry;
        configuredRegistry = builder.using(configuration).using(customVerifier).createConfiguredRegistry();
        assertThat(configuredRegistry).isNotNull();

        final SSLConnectionSocketFactory socketFactory =
                (SSLConnectionSocketFactory) configuredRegistry.lookup("https");
        assertThat(socketFactory).isNotNull();

        final Field hostnameVerifierField =
                getInaccessibleField(SSLConnectionSocketFactory.class, "hostnameVerifier");
        assertThat(hostnameVerifierField.get(socketFactory)).isSameAs(customVerifier);
    }

    @Test
    void canUseASystemHostnameVerifierByDefaultWhenTlsConfigurationNotSpecified() throws Exception {
        final Registry<ConnectionSocketFactory> configuredRegistry;
        configuredRegistry = builder.createConfiguredRegistry();
        assertThat(configuredRegistry).isNotNull();

        final SSLConnectionSocketFactory socketFactory =
                (SSLConnectionSocketFactory) configuredRegistry.lookup("https");
        assertThat(socketFactory).isNotNull();

        final Field hostnameVerifierField =
                getInaccessibleField(SSLConnectionSocketFactory.class, "hostnameVerifier");
        assertThat(hostnameVerifierField.get(socketFactory)).isInstanceOf(HostnameVerifier.class);
    }

    @Test
    void canUseASystemHostnameVerifierByDefaultWhenTlsConfigurationSpecified() throws Exception {
        final TlsConfiguration tlsConfiguration = new TlsConfiguration();
        tlsConfiguration.setVerifyHostname(true);
        configuration.setTlsConfiguration(tlsConfiguration);

        final Registry<ConnectionSocketFactory> configuredRegistry;
        configuredRegistry = builder.using(configuration).createConfiguredRegistry();
        assertThat(configuredRegistry).isNotNull();

        final SSLConnectionSocketFactory socketFactory =
                (SSLConnectionSocketFactory) configuredRegistry.lookup("https");
        assertThat(socketFactory).isNotNull();

        final Field hostnameVerifierField =
                getInaccessibleField(SSLConnectionSocketFactory.class, "hostnameVerifier");
        assertThat(hostnameVerifierField.get(socketFactory)).isInstanceOf(HostnameVerifier.class);
    }

    @Test
    void createClientCanPassCustomVerifierToApacheBuilder() throws Exception {
        final HostnameVerifier customVerifier = (s, sslSession) -> false;

        assertThat(builder.using(customVerifier).createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        final Field hostnameVerifierField =
                getInaccessibleField(org.apache.http.impl.client.HttpClientBuilder.class, "hostnameVerifier");
        assertThat(hostnameVerifierField.get(apacheBuilder)).isSameAs(customVerifier);
    }

    @Test
    void doesNotReuseConnectionsIfKeepAliveIsZero() throws Exception {
        configuration.setKeepAlive(Duration.seconds(0));
        assertThat(builder.using(configuration).createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        assertThat(spyHttpClientBuilderField("reuseStrategy", apacheBuilder))
                .isInstanceOf(NoConnectionReuseStrategy.class);
    }


    @Test
    void reusesConnectionsIfKeepAliveIsNonZero() throws Exception {
        configuration.setKeepAlive(Duration.seconds(1));
        assertThat(builder.using(configuration).createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        assertThat(spyHttpClientBuilderField("reuseStrategy", apacheBuilder))
                .isInstanceOf(DefaultConnectionReuseStrategy.class);
    }

    @Test
    void usesKeepAliveForPersistentConnections() throws Exception {
        configuration.setKeepAlive(Duration.seconds(1));
        assertThat(builder.using(configuration).createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        final DefaultConnectionKeepAliveStrategy strategy =
                (DefaultConnectionKeepAliveStrategy) spyHttpClientBuilderField("keepAliveStrategy", apacheBuilder);
        final HttpContext context = mock(HttpContext.class);
        final HttpResponse response = mock(HttpResponse.class);
        when(response.headerIterator(HTTP.CONN_KEEP_ALIVE)).thenReturn(mock(HeaderIterator.class));

        assertThat(strategy.getKeepAliveDuration(response, context)).isEqualTo(1000);
    }

    @Test
    void usesDefaultForNonPersistentConnections() throws Exception {
        configuration.setKeepAlive(Duration.seconds(1));
        assertThat(builder.using(configuration).createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        final Field field = getInaccessibleField(httpClientBuilderClass, "keepAliveStrategy");
        final DefaultConnectionKeepAliveStrategy strategy = (DefaultConnectionKeepAliveStrategy) field.get(apacheBuilder);
        final HttpContext context = mock(HttpContext.class);
        final HttpResponse response = mock(HttpResponse.class);
        final HeaderIterator iterator = new BasicListHeaderIterator(
                Collections.singletonList(new BasicHeader(HttpHeaders.CONNECTION, "timeout=50")),
                HttpHeaders.CONNECTION
        );
        when(response.headerIterator(HTTP.CONN_KEEP_ALIVE)).thenReturn(iterator);

        assertThat(strategy.getKeepAliveDuration(response, context)).isEqualTo(50_000);
    }

    @Test
    void ignoresCookiesByDefault() throws Exception {
        assertThat(builder.using(configuration).createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        assertThat(((RequestConfig) spyHttpClientBuilderField("defaultRequestConfig", apacheBuilder)).getCookieSpec())
                .isEqualTo(CookieSpecs.IGNORE_COOKIES);
    }

    @Test
    void usesBestMatchCookiePolicyIfCookiesAreEnabled() throws Exception {
        configuration.setCookiesEnabled(true);
        assertThat(builder.using(configuration).createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        assertThat(((RequestConfig) spyHttpClientBuilderField("defaultRequestConfig", apacheBuilder)).getCookieSpec())
                .isEqualTo(CookieSpecs.DEFAULT);
    }

    @Test
    void normalizeUriByDefault() throws Exception {
        assertThat(builder.using(configuration).createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        assertThat(((RequestConfig) spyHttpClientBuilderField("defaultRequestConfig", apacheBuilder)).isNormalizeUri())
            .isTrue();
    }

    @Test
    void disableNormalizeUriWhenDisabled() throws Exception {
        configuration.setNormalizeUriEnabled(false);
        assertThat(builder.using(configuration).createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        assertThat(((RequestConfig) spyHttpClientBuilderField("defaultRequestConfig", apacheBuilder)).isNormalizeUri())
            .isFalse();
    }

    @Test
    void setsTheSocketTimeout() throws Exception {
        configuration.setTimeout(Duration.milliseconds(500));
        assertThat(builder.using(configuration).createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        assertThat(((RequestConfig) spyHttpClientBuilderField("defaultRequestConfig", apacheBuilder)).getSocketTimeout())
                .isEqualTo(500);
    }

    @Test
    void setsTheConnectTimeout() throws Exception {
        configuration.setConnectionTimeout(Duration.milliseconds(500));
        assertThat(builder.using(configuration).createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        assertThat(((RequestConfig) spyHttpClientBuilderField("defaultRequestConfig", apacheBuilder)).getConnectTimeout())
                .isEqualTo(500);
    }

    @Test
    void setsTheConnectionRequestTimeout() throws Exception {
        configuration.setConnectionRequestTimeout(Duration.milliseconds(123));

        assertThat(builder.using(configuration).createClient(apacheBuilder, connectionManager, "test")).isNotNull();
        assertThat(((RequestConfig) spyHttpClientBuilderField("defaultRequestConfig", apacheBuilder)).getConnectionRequestTimeout())
                .isEqualTo(123);
    }

    @Test
    void disablesNaglesAlgorithm() throws Exception {
        assertThat(builder.using(configuration).createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        assertThat(((SocketConfig) spyHttpClientBuilderField("defaultSocketConfig", apacheBuilder)).isTcpNoDelay()).isTrue();
    }

    @Test
    void disablesStaleConnectionCheck() throws Exception {
        assertThat(builder.using(configuration).createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        // It is fine to use the isStaleConnectionCheckEnabled deprecated API, as we are ensuring
        // that the builder creates a client that does not check for stale connections on each
        // request, which adds significant overhead.
        assertThat(((RequestConfig) spyHttpClientBuilderField("defaultRequestConfig", apacheBuilder))
                .isStaleConnectionCheckEnabled()).isFalse();
    }

    @Test
    void usesTheDefaultRoutePlanner() throws Exception {
        final CloseableHttpClient httpClient = builder.using(configuration)
                .createClient(apacheBuilder, connectionManager, "test").getClient();

        assertThat(httpClient).isNotNull();
        assertThat(spyHttpClientBuilderField("routePlanner", apacheBuilder)).isNull();
        assertThat(spyHttpClientField("routePlanner", httpClient)).isInstanceOf(DefaultRoutePlanner.class);
    }

    @Test
    void usesACustomRoutePlanner() throws Exception {
        final HttpRoutePlanner routePlanner = new SystemDefaultRoutePlanner(new ProxySelector() {
            @Override
            public List<Proxy> select(URI uri) {
                return Collections.singletonList(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("192.168.52.1", 8080)));
            }

            @Override
            public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {

            }
        });
        final CloseableHttpClient httpClient = builder.using(configuration).using(routePlanner)
                .createClient(apacheBuilder, connectionManager, "test").getClient();

        assertThat(httpClient).isNotNull();
        assertThat(spyHttpClientBuilderField("routePlanner", apacheBuilder)).isSameAs(routePlanner);
        assertThat(spyHttpClientField("routePlanner", httpClient)).isSameAs(routePlanner);
    }

    @Test
    void usesACustomHttpRequestRetryHandler() throws Exception {
        final HttpRequestRetryHandler customHandler = (exception, executionCount, context) -> false;

        configuration.setRetries(1);
        assertThat(builder.using(configuration).using(customHandler)
                .createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        assertThat(spyHttpClientBuilderField("retryHandler", apacheBuilder)).isSameAs(customHandler);
    }

    @Test
    void usesCredentialsProvider() throws Exception {
        final CredentialsProvider credentialsProvider = new CredentialsProvider() {
            @Override
            public void setCredentials(AuthScope authscope, Credentials credentials) {
            }

            @Override
            @Nullable
            public Credentials getCredentials(AuthScope authscope) {
                return null;
            }

            @Override
            public void clear() {
            }
        };

        assertThat(builder.using(configuration).using(credentialsProvider)
                .createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        assertThat(spyHttpClientBuilderField("credentialsProvider", apacheBuilder)).isSameAs(credentialsProvider);
    }

    @Test
    void usesProxy() throws Exception {
        HttpClientConfiguration config = new HttpClientConfiguration();
        ProxyConfiguration proxy = new ProxyConfiguration("192.168.52.11", 8080);
        config.setProxyConfiguration(proxy);

        checkProxy(config, new HttpHost("dropwizard.io", 80), new HttpHost("192.168.52.11", 8080));
    }

    @Test
    void usesProxyWithoutPort() throws Exception {
        HttpClientConfiguration config = new HttpClientConfiguration();
        ProxyConfiguration proxy = new ProxyConfiguration("192.168.52.11");
        config.setProxyConfiguration(proxy);

        checkProxy(config, new HttpHost("dropwizard.io", 80), new HttpHost("192.168.52.11"));
    }

    @Test
    void usesProxyWithBasicAuth() throws Exception {
        HttpClientConfiguration config = new HttpClientConfiguration();
        AuthConfiguration auth = new AuthConfiguration("secret", "stuff");
        ProxyConfiguration proxy = new ProxyConfiguration("192.168.52.11", 8080, "http", auth);
        config.setProxyConfiguration(proxy);

        CloseableHttpClient httpClient = checkProxy(config, new HttpHost("dropwizard.io", 80),
                new HttpHost("192.168.52.11", 8080, "http"));
        CredentialsProvider credentialsProvider = (CredentialsProvider)
                getInaccessibleField(httpClient.getClass(), "credentialsProvider").get(httpClient);

        assertThat(credentialsProvider.getCredentials(new AuthScope("192.168.52.11", 8080)))
                .isEqualTo(new UsernamePasswordCredentials("secret", "stuff"));
    }

    @Test
    void usesProxyWithNtlmAuth() throws Exception {
        HttpClientConfiguration config = new HttpClientConfiguration();
        AuthConfiguration auth = new AuthConfiguration("secret", "stuff", "NTLM", "realm", "host", "domain", "NT");
        ProxyConfiguration proxy = new ProxyConfiguration("192.168.52.11", 8080, "http", auth);
        config.setProxyConfiguration(proxy);

        CloseableHttpClient httpClient = checkProxy(config, new HttpHost("dropwizard.io", 80),
                new HttpHost("192.168.52.11", 8080, "http"));
        CredentialsProvider credentialsProvider = (CredentialsProvider)
                getInaccessibleField(httpClient.getClass(), "credentialsProvider").get(httpClient);

        AuthScope authScope = new AuthScope("192.168.52.11", 8080, "realm", "NTLM");
        Credentials credentials = new NTCredentials("secret", "stuff", "host", "domain");

        assertThat(credentialsProvider.getCredentials(authScope))
                .isEqualTo(credentials);
    }

    @Test
    void usesProxyWithNonProxyHosts() throws Exception {
        HttpClientConfiguration config = new HttpClientConfiguration();
        ProxyConfiguration proxy = new ProxyConfiguration("192.168.52.11", 8080);
        proxy.setNonProxyHosts(Collections.singletonList("*.example.com"));
        config.setProxyConfiguration(proxy);

        checkProxy(config, new HttpHost("host.example.com", 80), null);
    }

    @Test
    void usesProxyWithNonProxyHostsAndTargetDoesNotMatch() throws Exception {
        HttpClientConfiguration config = new HttpClientConfiguration();
        ProxyConfiguration proxy = new ProxyConfiguration("192.168.52.11");
        proxy.setNonProxyHosts(Collections.singletonList("*.example.com"));
        config.setProxyConfiguration(proxy);

        checkProxy(config, new HttpHost("dropwizard.io", 80), new HttpHost("192.168.52.11"));
    }

    @Test
    void usesNoProxy() throws Exception {
        checkProxy(new HttpClientConfiguration(), new HttpHost("dropwizard.io", 80), null);
    }

    private CloseableHttpClient checkProxy(HttpClientConfiguration config, HttpHost target,
                                           @Nullable HttpHost expectedProxy) throws Exception {
        CloseableHttpClient httpClient = builder.using(config).build("test");
        HttpRoutePlanner routePlanner = (HttpRoutePlanner)
                getInaccessibleField(httpClient.getClass(), "routePlanner").get(httpClient);

        HttpRoute route = routePlanner.determineRoute(target, new HttpGet(target.toURI()),
                new BasicHttpContext());
        assertThat(route.getProxyHost()).isEqualTo(expectedProxy);
        assertThat(route.getTargetHost()).isEqualTo(target);
        assertThat(route.getHopCount()).isEqualTo(expectedProxy != null ? 2 : 1);

        return httpClient;
    }

    @Test
    void setValidateAfterInactivityPeriodFromConfiguration() throws Exception {
        int validateAfterInactivityPeriod = 50000;
        configuration.setValidateAfterInactivityPeriod(Duration.milliseconds(validateAfterInactivityPeriod));
        final ConfiguredCloseableHttpClient client = builder.using(configuration)
                .createClient(apacheBuilder, builder.configureConnectionManager(connectionManager), "test");

        assertThat(client).isNotNull();
        assertThat(spyHttpClientBuilderField("connManager", apacheBuilder)).isSameAs(connectionManager);
        verify(connectionManager).setValidateAfterInactivity(validateAfterInactivityPeriod);
    }

    @Test
    void usesACustomHttpClientMetricNameStrategy() throws Exception {
        assertThat(builder.using(HttpClientMetricNameStrategies.HOST_AND_METHOD)
                .createClient(apacheBuilder, connectionManager, "test"))
                .isNotNull();
        assertThat(getInaccessibleField(InstrumentedHttpRequestExecutor.class,"metricNameStrategy")
                .get(spyHttpClientBuilderField("requestExec", apacheBuilder)))
                .isSameAs(HttpClientMetricNameStrategies.HOST_AND_METHOD);
    }

    @Test
    void usesMethodOnlyHttpClientMetricNameStrategyByDefault() throws Exception {
        assertThat(builder.createClient(apacheBuilder, connectionManager, "test"))
                .isNotNull();
        assertThat(getInaccessibleField(InstrumentedHttpRequestExecutor.class, "metricNameStrategy")
                .get(spyHttpClientBuilderField("requestExec", apacheBuilder)))
                .isSameAs(HttpClientMetricNameStrategies.METHOD_ONLY);
    }

    @Test
    void exposedConfigIsTheSameAsInternalToTheWrappedHttpClient() throws Exception {
        ConfiguredCloseableHttpClient client = builder.createClient(apacheBuilder, connectionManager, "test");
        assertThat(client).isNotNull();

        assertThat(spyHttpClientField("defaultConfig", client.getClient())).isEqualTo(client.getDefaultRequestConfig());
    }

    @Test
    void disablesContentCompression() throws Exception {
        ConfiguredCloseableHttpClient client = builder
                .disableContentCompression(true)
                .createClient(apacheBuilder, connectionManager, "test");
        assertThat(client).isNotNull();

        final Boolean contentCompressionDisabled =
            (Boolean) getInaccessibleField(httpClientBuilderClass, "contentCompressionDisabled").get(apacheBuilder);
        assertThat(contentCompressionDisabled).isTrue();
    }

    @Test
    void managedByEnvironment() throws Exception {
        final Environment environment = mock(Environment.class);
        when(environment.getName()).thenReturn("test-env");
        when(environment.metrics()).thenReturn(new MetricRegistry());

        final LifecycleEnvironment lifecycle = mock(LifecycleEnvironment.class);
        when(environment.lifecycle()).thenReturn(lifecycle);

        final CloseableHttpClient httpClient = mock(CloseableHttpClient.class);
        HttpClientBuilder httpClientBuilder = spy(new HttpClientBuilder(environment));
        when(httpClientBuilder.buildWithDefaultRequestConfiguration("test-apache-client"))
                .thenReturn(new ConfiguredCloseableHttpClient(httpClient, RequestConfig.DEFAULT));
        assertThat(httpClientBuilder.build("test-apache-client")).isSameAs(httpClient);

        // Verify that we registered the managed object
        final ArgumentCaptor<Managed> argumentCaptor = ArgumentCaptor.forClass(Managed.class);
        verify(lifecycle).manage(argumentCaptor.capture());

        // Verify that the managed object actually stops the HTTP client
        final Managed managed = argumentCaptor.getValue();
        managed.stop();
        verify(httpClient).close();
    }

    @Test
    void usesACustomRedirectStrategy() throws Exception {
        RedirectStrategy neverFollowRedirectStrategy = new RedirectStrategy() {
            @Override
            public boolean isRedirected(HttpRequest httpRequest,
                                        HttpResponse httpResponse,
                                        HttpContext httpContext) {
                return false;
            }

            @Override
            @Nullable
            public HttpUriRequest getRedirect(HttpRequest httpRequest,
                                              HttpResponse httpResponse,
                                              HttpContext httpContext) {
                return null;
            }
        };
        ConfiguredCloseableHttpClient client = builder.using(neverFollowRedirectStrategy)
                                                      .createClient(apacheBuilder, connectionManager, "test");
        assertThat(client).isNotNull();
        assertThat(spyHttpClientBuilderField("redirectStrategy", apacheBuilder)).isSameAs(neverFollowRedirectStrategy);
    }

    @Test
    void usesDefaultHeaders() throws Exception {
        final ConfiguredCloseableHttpClient client =
                builder.using(Collections.singletonList(new BasicHeader(HttpHeaders.ACCEPT_LANGUAGE, "de")))
                        .createClient(apacheBuilder, connectionManager, "test");
        assertThat(client).isNotNull();

        @SuppressWarnings("unchecked")
        List<? extends Header> defaultHeaders =
            (List<? extends Header>) getInaccessibleField(httpClientBuilderClass, "defaultHeaders")
                .get(apacheBuilder);

        assertThat(defaultHeaders)
            .singleElement()
            .satisfies(header -> assertThat(header.getName()).isEqualTo(HttpHeaders.ACCEPT_LANGUAGE))
            .satisfies(header -> assertThat(header.getValue()).isEqualTo("de"));
    }

    @Test
    void usesHttpProcessor() throws Exception {
        HttpProcessor httpProcessor = mock(HttpProcessor.class);
        final ConfiguredCloseableHttpClient client =
            builder.using(httpProcessor)
                .createClient(apacheBuilder, connectionManager, "test");
        assertThat(client).isNotNull();
        assertThat(getInaccessibleField(httpClientBuilderClass, "httpprocessor")
            .get(apacheBuilder))
            .isSameAs(httpProcessor);
    }

    @Test
    void usesServiceUnavailableRetryStrategy() throws Exception {
        ServiceUnavailableRetryStrategy serviceUnavailableRetryStrategy = mock(ServiceUnavailableRetryStrategy.class);
        final ConfiguredCloseableHttpClient client =
            builder.using(serviceUnavailableRetryStrategy)
                .createClient(apacheBuilder, connectionManager, "test");
        assertThat(client).isNotNull();
        assertThat(getInaccessibleField(httpClientBuilderClass, "serviceUnavailStrategy")
            .get(apacheBuilder))
            .isSameAs(serviceUnavailableRetryStrategy);
    }

    @Test
    void allowsCustomBuilderConfiguration() throws Exception {
        CustomBuilder builder = new CustomBuilder(new MetricRegistry());
        assertThat(builder.customized).isFalse();
        builder.createClient(apacheBuilder, connectionManager, "test");
        assertThat(builder.customized).isTrue();
        assertThat(getInaccessibleField(httpClientBuilderClass, "requestExec").get(apacheBuilder))
            .isInstanceOf(CustomRequestExecutor.class);
    }

    @Test
    void buildWithAnotherBuilder() throws Exception {
        CustomBuilder builder = new CustomBuilder(new MetricRegistry(), anotherApacheBuilder);
        builder.build("test");
        assertThat(getInaccessibleField(httpClientBuilderClass, "requestExec").get(anotherApacheBuilder))
            .isInstanceOf(CustomRequestExecutor.class);
    }

    @Test
    void configureCredentialReturnsNTCredentialsForNTLMConfig() {
        assertThat(builder.configureCredentials(new AuthConfiguration("username", "password", "NTLM", "realm", "hostname", "domain", "NT")))
            .isInstanceOfSatisfying(NTCredentials.class, credentials -> assertThat(credentials)
                .satisfies(c -> assertThat(c.getPassword()).isEqualTo("password"))
                .satisfies(c -> assertThat(c.getUserPrincipal().getName()).isEqualTo("DOMAIN\\username")));
    }

    @Test
    void configureCredentialReturnsUserNamePasswordCredentialsForBasicConfig() {
        assertThat(builder.configureCredentials(new AuthConfiguration("username", "password")))
            .isInstanceOfSatisfying(UsernamePasswordCredentials.class, upCredentials -> assertThat(upCredentials)
                .satisfies(c -> assertThat(c.getPassword()).isEqualTo("password"))
                .satisfies(c -> assertThat(c.getUserPrincipal().getName()).isEqualTo("username")));
    }

    private Object spyHttpClientBuilderField(final String fieldName, final Object obj) throws Exception {
        final Field field = getInaccessibleField(httpClientBuilderClass, fieldName);
        return field.get(obj);
    }

    private Object spyHttpClientField(final String fieldName, final Object obj) throws Exception {
        final Field field = getInaccessibleField(httpClientClass, fieldName);
        return field.get(obj);
    }

    private static Field getInaccessibleField(Class klass, String name) throws NoSuchFieldException {
        Field field = klass.getDeclaredField(name);
        field.setAccessible(true);
        return field;
    }
}
