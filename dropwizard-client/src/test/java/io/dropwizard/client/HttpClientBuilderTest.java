package io.dropwizard.client;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.httpclient5.HttpClientMetricNameStrategies;
import com.codahale.metrics.httpclient5.InstrumentedHttpClientConnectionManager;
import com.codahale.metrics.httpclient5.InstrumentedHttpRequestExecutor;
import io.dropwizard.client.proxy.AuthConfiguration;
import io.dropwizard.client.proxy.ProxyConfiguration;
import io.dropwizard.client.ssl.TlsConfiguration;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.util.Duration;
import org.apache.hc.client5.http.DnsResolver;
import org.apache.hc.client5.http.HttpRequestRetryStrategy;
import org.apache.hc.client5.http.HttpRoute;
import org.apache.hc.client5.http.SystemDefaultDnsResolver;
import org.apache.hc.client5.http.auth.AuthScope;
import org.apache.hc.client5.http.auth.Credentials;
import org.apache.hc.client5.http.auth.CredentialsProvider;
import org.apache.hc.client5.http.auth.CredentialsStore;
import org.apache.hc.client5.http.auth.NTCredentials;
import org.apache.hc.client5.http.auth.UsernamePasswordCredentials;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.cookie.StandardCookieSpec;
import org.apache.hc.client5.http.impl.DefaultConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.routing.DefaultRoutePlanner;
import org.apache.hc.client5.http.impl.routing.SystemDefaultRoutePlanner;
import org.apache.hc.client5.http.protocol.HttpClientContext;
import org.apache.hc.client5.http.protocol.RedirectStrategy;
import org.apache.hc.client5.http.routing.HttpRoutePlanner;
import org.apache.hc.client5.http.socket.ConnectionSocketFactory;
import org.apache.hc.client5.http.socket.PlainConnectionSocketFactory;
import org.apache.hc.client5.http.ssl.SSLConnectionSocketFactory;
import org.apache.hc.core5.http.ConnectionReuseStrategy;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpException;
import org.apache.hc.core5.http.HttpHeaders;
import org.apache.hc.core5.http.HttpHost;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.HttpResponse;
import org.apache.hc.core5.http.config.Registry;
import org.apache.hc.core5.http.config.RegistryBuilder;
import org.apache.hc.core5.http.impl.DefaultConnectionReuseStrategy;
import org.apache.hc.core5.http.impl.io.HttpRequestExecutor;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.BasicHeaderIterator;
import org.apache.hc.core5.http.protocol.BasicHttpContext;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.http.protocol.HttpProcessor;
import org.apache.hc.core5.util.TimeValue;
import org.apache.hc.core5.util.Timeout;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.net.ssl.HostnameVerifier;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.validateMockitoUsage;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AnotherHttpClientBuilder extends org.apache.hc.client5.http.impl.classic.HttpClientBuilder {
    public static AnotherHttpClientBuilder create() {
        return new AnotherHttpClientBuilder();
    }
}

class HttpClientBuilderTest {
    static class CustomRequestExecutor extends HttpRequestExecutor {
    }

    static class CustomBuilder extends HttpClientBuilder {
        public boolean customized;
        public org.apache.hc.client5.http.impl.classic.HttpClientBuilder builder;

        public CustomBuilder(MetricRegistry metricRegistry) {
            this(metricRegistry, org.apache.hc.client5.http.impl.classic.HttpClientBuilder.create());
        }

        public CustomBuilder(MetricRegistry metricRegistry, org.apache.hc.client5.http.impl.classic.HttpClientBuilder builder) {
            super(metricRegistry);
            customized = false;
            this.builder = builder;
        }

        @Override
        protected org.apache.hc.client5.http.impl.classic.HttpClientBuilder createBuilder() {
            return builder;
        }

        @Override
        protected HttpRequestExecutor createRequestExecutor(String name) {
            return new CustomRequestExecutor();
        }

        @Override
        protected org.apache.hc.client5.http.impl.classic.HttpClientBuilder customizeBuilder(
                org.apache.hc.client5.http.impl.classic.HttpClientBuilder builder
        ) {
            customized = true;
            return builder;
        }
    }

    private final Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
            .register("http", PlainConnectionSocketFactory.getSocketFactory())
            .register("https", SSLConnectionSocketFactory.getSocketFactory())
            .build();
    private HttpClientConfiguration configuration;
    private HttpClientBuilder builder;
    private InstrumentedHttpClientConnectionManager connectionManager;
    private org.apache.hc.client5.http.impl.classic.HttpClientBuilder apacheBuilder;
    private AnotherHttpClientBuilder anotherApacheBuilder;

    @BeforeEach
    void setUp() {
        final MetricRegistry metricRegistry = new MetricRegistry();
        configuration = new HttpClientConfiguration();
        builder = new HttpClientBuilder(metricRegistry);
        connectionManager = spy(InstrumentedHttpClientConnectionManager.builder(metricRegistry).socketFactoryRegistry(registry).build());
        apacheBuilder = org.apache.hc.client5.http.impl.classic.HttpClientBuilder.create();
        anotherApacheBuilder = spy(AnotherHttpClientBuilder.create());
    }

    @AfterEach
    void validate() {
        validateMockitoUsage();
    }

    @Test
    void setsTheMaximumConnectionPoolSize() {
        configuration.setMaxConnections(412);
        final ConfiguredCloseableHttpClient client = builder.using(configuration)
                .createClient(apacheBuilder, builder.configureConnectionManager(connectionManager), "test");

        assertThat(client).isNotNull();
        assertThat(apacheBuilder).extracting("connManager").isSameAs(connectionManager);
        verify(connectionManager).setMaxTotal(412);
    }


    @Test
    void setsTheMaximumRoutePoolSize() {
        configuration.setMaxConnectionsPerRoute(413);
        final ConfiguredCloseableHttpClient client = builder.using(configuration)
                .createClient(apacheBuilder, builder.configureConnectionManager(connectionManager), "test");

        assertThat(client).isNotNull();
        assertThat(apacheBuilder).extracting("connManager").isSameAs(connectionManager);
        verify(connectionManager).setDefaultMaxPerRoute(413);
    }

    @Test
    void setsTheUserAgent() {
        configuration.setUserAgent(Optional.of("qwerty"));
        assertThat(builder.using(configuration).createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        assertThat(apacheBuilder).extracting("userAgent").isEqualTo("qwerty");
    }

    @Test
    void canUseACustomDnsResolver() {
        final DnsResolver resolver = mock(DnsResolver.class);
        final InstrumentedHttpClientConnectionManager manager =
                builder.using(resolver).createConnectionManager(registry, "test");

        assertThat(manager)
                .extracting("connectionOperator")
                .extracting("dnsResolver")
                .isEqualTo(resolver);
    }


    @Test
    void usesASystemDnsResolverByDefault() {
        final InstrumentedHttpClientConnectionManager manager = builder.createConnectionManager(registry, "test");

        assertThat(manager)
                .extracting("connectionOperator")
                .extracting("dnsResolver")
                .isInstanceOf(SystemDefaultDnsResolver.class);
    }

    @Test
    void canUseACustomHostnameVerifierWhenTlsConfigurationNotSpecified() {
        final HostnameVerifier customVerifier = (s, sslSession) -> false;

        final Registry<ConnectionSocketFactory> configuredRegistry;
        configuredRegistry = builder.using(customVerifier).createConfiguredRegistry();
        assertThat(configuredRegistry).isNotNull();

        final SSLConnectionSocketFactory socketFactory =
                (SSLConnectionSocketFactory) configuredRegistry.lookup("https");

        assertThat(socketFactory)
                .isNotNull()
                .extracting("hostnameVerifier").isSameAs(customVerifier);
    }

    @Test
    void canUseACustomHostnameVerifierWhenTlsConfigurationSpecified() {
        final TlsConfiguration tlsConfiguration = new TlsConfiguration();
        tlsConfiguration.setVerifyHostname(true);
        configuration.setTlsConfiguration(tlsConfiguration);

        final HostnameVerifier customVerifier = (s, sslSession) -> false;

        final Registry<ConnectionSocketFactory> configuredRegistry;
        configuredRegistry = builder.using(configuration).using(customVerifier).createConfiguredRegistry();
        assertThat(configuredRegistry).isNotNull();

        final SSLConnectionSocketFactory socketFactory =
                (SSLConnectionSocketFactory) configuredRegistry.lookup("https");
        assertThat(socketFactory)
                .isNotNull()
                .extracting("hostnameVerifier").isSameAs(customVerifier);
    }

    @Test
    void canUseASystemHostnameVerifierByDefaultWhenTlsConfigurationNotSpecified() {
        final Registry<ConnectionSocketFactory> configuredRegistry;
        configuredRegistry = builder.createConfiguredRegistry();
        assertThat(configuredRegistry).isNotNull();

        final SSLConnectionSocketFactory socketFactory =
                (SSLConnectionSocketFactory) configuredRegistry.lookup("https");
        assertThat(socketFactory)
                .isNotNull()
                .extracting("hostnameVerifier")
                .isInstanceOf(HostnameVerifier.class);
    }

    @Test
    void canUseASystemHostnameVerifierByDefaultWhenTlsConfigurationSpecified() {
        final TlsConfiguration tlsConfiguration = new TlsConfiguration();
        tlsConfiguration.setVerifyHostname(true);
        configuration.setTlsConfiguration(tlsConfiguration);

        final Registry<ConnectionSocketFactory> configuredRegistry;
        configuredRegistry = builder.using(configuration).createConfiguredRegistry();
        assertThat(configuredRegistry).isNotNull();

        final SSLConnectionSocketFactory socketFactory =
                (SSLConnectionSocketFactory) configuredRegistry.lookup("https");
        assertThat(socketFactory)
                .isNotNull()
                .extracting("hostnameVerifier")
                .isInstanceOf(HostnameVerifier.class);
    }

    @Test
    void doesNotReuseConnectionsIfKeepAliveIsZero() {
        configuration.setKeepAlive(Duration.seconds(0));
        assertThat(builder.using(configuration).createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        assertThat(apacheBuilder).extracting("reuseStrategy")
                .isInstanceOf(ConnectionReuseStrategy.class);
    }


    @Test
    void reusesConnectionsIfKeepAliveIsNonZero() {
        configuration.setKeepAlive(Duration.seconds(1));
        assertThat(builder.using(configuration).createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        assertThat(apacheBuilder).extracting("reuseStrategy")
                .isInstanceOf(DefaultConnectionReuseStrategy.class);
    }

    @Test
    void usesKeepAliveForPersistentConnections() {
        configuration.setKeepAlive(Duration.seconds(1));
        final ConfiguredCloseableHttpClient client = builder.using(configuration).createClient(apacheBuilder, connectionManager, "test");
        assertThat(client).isNotNull();

        final HttpClientContext context = mock(HttpClientContext.class);
        final HttpResponse response = mock(HttpResponse.class);
        when(context.getRequestConfig()).thenReturn(client.getDefaultRequestConfig());
        when(context.getRequestConfigOrDefault()).thenCallRealMethod();
        when(response.headerIterator()).thenReturn(Collections.emptyIterator());
        when(response.headerIterator(any())).thenReturn(Collections.emptyIterator());

        assertThat(apacheBuilder).extracting("keepAliveStrategy")
                .isInstanceOfSatisfying(DefaultConnectionKeepAliveStrategy.class,
                        strategy -> assertThat(strategy.getKeepAliveDuration(response, context)).isEqualByComparingTo(TimeValue.ofSeconds(1)));
    }

    @Test
    void usesDefaultForNonPersistentConnections() {
        configuration.setKeepAlive(Duration.seconds(1));
        assertThat(builder.using(configuration).createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        final HttpContext context = mock(HttpContext.class);
        final HttpResponse response = mock(HttpResponse.class);
        BasicHeaderIterator basicHeaderIterator = new BasicHeaderIterator(
                new Header[]{new BasicHeader(HttpHeaders.CONNECTION, "timeout=50")},
                HttpHeaders.CONNECTION
        );
        when(response.headerIterator()).thenReturn(basicHeaderIterator);
        when(response.headerIterator(any())).thenReturn(basicHeaderIterator);

        assertThat(apacheBuilder).extracting("keepAliveStrategy")
                .isInstanceOfSatisfying(DefaultConnectionKeepAliveStrategy.class,
                        strategy -> assertThat(strategy.getKeepAliveDuration(response, context)).isEqualByComparingTo(TimeValue.ofMilliseconds(50_000L)));
    }

    @Test
    void ignoresCookiesByDefault() {
        assertThat(builder.using(configuration).createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        assertThat(apacheBuilder)
                .extracting("defaultRequestConfig")
                .isInstanceOfSatisfying(RequestConfig.class, requestConfig ->
                        assertThat(requestConfig.getCookieSpec()).isEqualTo(StandardCookieSpec.IGNORE));
    }

    @Test
    void usesBestMatchCookiePolicyIfCookiesAreEnabled() {
        configuration.setCookiesEnabled(true);
        assertThat(builder.using(configuration).createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        assertThat(apacheBuilder)
                .extracting("defaultRequestConfig")
                .isInstanceOfSatisfying(RequestConfig.class, requestConfig ->
                        assertThat(requestConfig.getCookieSpec()).isEqualTo(StandardCookieSpec.RELAXED));
    }

    @Test
    void setsTheSocketTimeout() {
        configuration.setTimeout(Duration.milliseconds(500));
        assertThat(builder.using(configuration).createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        assertThat(apacheBuilder)
                .extracting("defaultRequestConfig")
                .isInstanceOfSatisfying(RequestConfig.class, requestConfig ->
                        assertThat(requestConfig.getResponseTimeout()).isEqualTo(Timeout.ofMilliseconds(500L)));
    }

    @Test
    void setsTheConnectTimeout() {
        configuration.setConnectionTimeout(Duration.milliseconds(500));
        assertThat(builder.using(configuration).createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        assertThat(apacheBuilder)
                .extracting("defaultRequestConfig")
                .isInstanceOfSatisfying(RequestConfig.class, requestConfig ->
                        assertThat(requestConfig.getConnectTimeout()).isEqualTo(Timeout.ofMilliseconds(500L)));
    }

    @Test
    void setsTheConnectionRequestTimeout() {
        configuration.setConnectionRequestTimeout(Duration.milliseconds(123));

        assertThat(builder.using(configuration).createClient(apacheBuilder, connectionManager, "test")).isNotNull();
        assertThat(apacheBuilder)
                .extracting("defaultRequestConfig")
                .isInstanceOfSatisfying(RequestConfig.class, requestConfig ->
                        assertThat(requestConfig.getConnectionRequestTimeout()).isEqualTo(Timeout.ofMilliseconds(123L)));
    }

    @Test
    void usesTheDefaultRoutePlanner() {
        final CloseableHttpClient httpClient = builder.using(configuration)
                .createClient(apacheBuilder, connectionManager, "test").getClient();

        assertThat(apacheBuilder).extracting("routePlanner").isNull();
        assertThat(httpClient).isNotNull();
        assertThat(httpClient).extracting("routePlanner").isInstanceOf(DefaultRoutePlanner.class);
    }

    @Test
    void usesACustomRoutePlanner() {
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

        assertThat(apacheBuilder).extracting("routePlanner").isSameAs(routePlanner);
        assertThat(httpClient).isNotNull();
        assertThat(httpClient).extracting("routePlanner").isSameAs(routePlanner);
    }

    @Test
    void usesACustomHttpRequestRetryHandler() {
        final HttpRequestRetryStrategy customHandler = new HttpRequestRetryStrategy() {
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

        configuration.setRetries(1);
        assertThat(builder.using(configuration).using(customHandler)
                .createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        assertThat(apacheBuilder).extracting("retryStrategy").isSameAs(customHandler);
    }

    @Test
    void usesCredentialsProvider() {
        final CredentialsStore credentialsProvider = new CredentialsStore() {
            @Override
            public void setCredentials(AuthScope authscope, Credentials credentials) {
            }

            @Override
            @Nullable
            public Credentials getCredentials(AuthScope authScope, HttpContext context) {
                return null;
            }

            @Override
            public void clear() {
            }
        };

        assertThat(builder.using(configuration).using(credentialsProvider)
                .createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        assertThat(apacheBuilder).extracting("credentialsProvider").isSameAs(credentialsProvider);
    }

    @Test
    void usesProxy() {
        HttpClientConfiguration config = new HttpClientConfiguration();
        ProxyConfiguration proxy = new ProxyConfiguration("192.168.52.11", 8080);
        config.setProxyConfiguration(proxy);

        checkProxy(config, new HttpHost("dropwizard.io", 80), new HttpHost("192.168.52.11", 8080));
    }

    @Test
    void usesProxyWithoutPort() {
        HttpClientConfiguration config = new HttpClientConfiguration();
        ProxyConfiguration proxy = new ProxyConfiguration("192.168.52.11");
        config.setProxyConfiguration(proxy);

        checkProxy(config, new HttpHost("dropwizard.io", 80), new HttpHost("192.168.52.11"));
    }

    @Test
    void usesProxyWithBasicAuth() {
        HttpClientConfiguration config = new HttpClientConfiguration();
        AuthConfiguration auth = new AuthConfiguration("secret", "stuff");
        ProxyConfiguration proxy = new ProxyConfiguration("192.168.52.11", 8080, "http", auth);
        config.setProxyConfiguration(proxy);

        CloseableHttpClient httpClient = checkProxy(config, new HttpHost("dropwizard.io", 80),
                new HttpHost("http", "192.168.52.11", 8080));

        final AuthScope authScope = new AuthScope("192.168.52.11", 8080);
        final HttpContext httpContext = mock(HttpContext.class);
        final Credentials credentials = new UsernamePasswordCredentials("secret", "stuff".toCharArray());
        assertThat(httpClient).extracting("credentialsProvider")
                .isInstanceOfSatisfying(CredentialsProvider.class, credentialsProvider ->
                        assertThat(credentialsProvider.getCredentials(authScope, httpContext)).isEqualTo(credentials));
    }

    @Test
    void usesProxyWithNtlmAuth() {
        HttpClientConfiguration config = new HttpClientConfiguration();
        AuthConfiguration auth = new AuthConfiguration("secret", "stuff", "NTLM", "realm", "host", "domain", "NT");
        ProxyConfiguration proxy = new ProxyConfiguration("192.168.52.11", 8080, "http", auth);
        config.setProxyConfiguration(proxy);

        CloseableHttpClient httpClient = checkProxy(config, new HttpHost("dropwizard.io", 80),
                new HttpHost("http", "192.168.52.11", 8080));

        AuthScope authScope = new AuthScope(null, "192.168.52.11", 8080, "realm", "NTLM");
        Credentials credentials = new NTCredentials("secret", "stuff".toCharArray(), "host", "domain");

        assertThat(httpClient).extracting("credentialsProvider")
                .isInstanceOfSatisfying(CredentialsProvider.class, credentialsProvider ->
                        assertThat(credentialsProvider.getCredentials(authScope, mock(HttpContext.class))).isEqualTo(credentials));
    }

    @Test
    void usesProxyWithNonProxyHosts() {
        HttpClientConfiguration config = new HttpClientConfiguration();
        ProxyConfiguration proxy = new ProxyConfiguration("192.168.52.11", 8080);
        proxy.setNonProxyHosts(Collections.singletonList("*.example.com"));
        config.setProxyConfiguration(proxy);

        checkProxy(config, new HttpHost("host.example.com", 80), null);
    }

    @Test
    void usesProxyWithNonProxyHostsAndTargetDoesNotMatch() {
        HttpClientConfiguration config = new HttpClientConfiguration();
        ProxyConfiguration proxy = new ProxyConfiguration("192.168.52.11");
        proxy.setNonProxyHosts(Collections.singletonList("*.example.com"));
        config.setProxyConfiguration(proxy);

        checkProxy(config, new HttpHost("dropwizard.io", 80), new HttpHost("192.168.52.11"));
    }

    @Test
    void usesNoProxy() {
        checkProxy(new HttpClientConfiguration(), new HttpHost("dropwizard.io", 80), null);
    }

    private CloseableHttpClient checkProxy(HttpClientConfiguration config, HttpHost target,
                                           @Nullable HttpHost expectedProxy) {
        CloseableHttpClient httpClient = builder.using(config).build("test");

        assertThat(httpClient).extracting("routePlanner")
                        .isInstanceOfSatisfying(HttpRoutePlanner.class, routePlanner -> {
                            HttpRoute route;
                            try {
                                route = routePlanner.determineRoute(target, new BasicHttpContext());
                            } catch (HttpException e) {
                                throw new RuntimeException(e);
                            }
                            assertThat(route.getProxyHost()).isEqualTo(expectedProxy);
                            assertThat(route.getTargetHost()).isEqualTo(target);
                            assertThat(route.getHopCount()).isEqualTo(expectedProxy != null ? 2 : 1);
                        });

        return httpClient;
    }

    @Test
    void setValidateAfterInactivityPeriodFromConfiguration() {
        int validateAfterInactivityPeriod = 50000;
        configuration.setValidateAfterInactivityPeriod(Duration.milliseconds(validateAfterInactivityPeriod));
        final ConfiguredCloseableHttpClient client = builder.using(configuration)
                .createClient(apacheBuilder, builder.configureConnectionManager(connectionManager), "test");

        assertThat(client).isNotNull();
        assertThat(apacheBuilder).extracting("connManager").isSameAs(connectionManager);
        verify(connectionManager).setValidateAfterInactivity(TimeValue.ofMilliseconds(validateAfterInactivityPeriod));
    }

    @Test
    void usesACustomHttpClientMetricNameStrategy() {
        assertThat(builder.using(HttpClientMetricNameStrategies.HOST_AND_METHOD)
                .createClient(apacheBuilder, connectionManager, "test"))
                .isNotNull();
        assertThat(apacheBuilder).extracting("requestExec")
                .isInstanceOfSatisfying(InstrumentedHttpRequestExecutor.class, executor ->
                        assertThat(executor).extracting("metricNameStrategy").isSameAs(HttpClientMetricNameStrategies.HOST_AND_METHOD));
    }

    @Test
    void usesMethodOnlyHttpClientMetricNameStrategyByDefault() {
        assertThat(builder.createClient(apacheBuilder, connectionManager, "test"))
                .isNotNull();
        assertThat(apacheBuilder).extracting("requestExec")
                .isInstanceOfSatisfying(InstrumentedHttpRequestExecutor.class, executor ->
                        assertThat(executor).extracting("metricNameStrategy").isSameAs(HttpClientMetricNameStrategies.METHOD_ONLY));
    }

    @Test
    void exposedConfigIsTheSameAsInternalToTheWrappedHttpClient() {
        ConfiguredCloseableHttpClient client = builder.createClient(apacheBuilder, connectionManager, "test");
        assertThat(client).isNotNull();

        assertThat(client.getClient()).extracting("defaultConfig").isEqualTo(client.getDefaultRequestConfig());
    }

    @Test
    void disablesContentCompression() {
        ConfiguredCloseableHttpClient client = builder
                .disableContentCompression(true)
                .createClient(apacheBuilder, connectionManager, "test");
        assertThat(client).isNotNull();
        assertThat(apacheBuilder)
                .extracting("contentCompressionDisabled", InstanceOfAssertFactories.BOOLEAN).isTrue();
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
    void usesACustomRedirectStrategy() {
        RedirectStrategy neverFollowRedirectStrategy = new RedirectStrategy() {
            @Override
            public boolean isRedirected(HttpRequest httpRequest,
                                        HttpResponse httpResponse,
                                        HttpContext httpContext) {
                return false;
            }

            @Override
            @Nullable
            public URI getLocationURI(HttpRequest request, HttpResponse response, HttpContext context) {
                return null;
            }
        };
        ConfiguredCloseableHttpClient client = builder.using(neverFollowRedirectStrategy)
                .createClient(apacheBuilder, connectionManager, "test");
        assertThat(client).isNotNull();
        assertThat(apacheBuilder).extracting("redirectStrategy").isSameAs(neverFollowRedirectStrategy);
    }

    @Test
    void usesDefaultHeaders() {
        final ConfiguredCloseableHttpClient client =
                builder.using(Collections.singletonList(new BasicHeader(HttpHeaders.ACCEPT_LANGUAGE, "de")))
                        .createClient(apacheBuilder, connectionManager, "test");
        assertThat(client).isNotNull();

        assertThat(apacheBuilder)
                .extracting("defaultHeaders")
                .asInstanceOf(InstanceOfAssertFactories.list(Header.class))
                .singleElement()
                .satisfies(header -> assertThat(header.getName()).isEqualTo(HttpHeaders.ACCEPT_LANGUAGE))
                .satisfies(header -> assertThat(header.getValue()).isEqualTo("de"));
    }

    @Test
    void usesHttpProcessor() {
        HttpProcessor httpProcessor = mock(HttpProcessor.class);
        final ConfiguredCloseableHttpClient client =
                builder.using(httpProcessor)
                        .createClient(apacheBuilder, connectionManager, "test");
        assertThat(client).isNotNull();
        assertThat(apacheBuilder).extracting("requestInterceptors")
                .asInstanceOf(InstanceOfAssertFactories.LIST)
                .satisfies(requestInterceptors -> assertThat(requestInterceptors).hasSize(1));
        assertThat(apacheBuilder).extracting("responseInterceptors")
                .asInstanceOf(InstanceOfAssertFactories.LIST)
                .satisfies(responseInterceptors -> assertThat(responseInterceptors).hasSize(1));
    }

    @Test
    void allowsCustomBuilderConfiguration() {
        CustomBuilder builder = new CustomBuilder(new MetricRegistry());
        assertThat(builder.customized).isFalse();
        builder.createClient(apacheBuilder, connectionManager, "test");
        assertThat(builder.customized).isTrue();
        assertThat(apacheBuilder).extracting("requestExec")
                .isInstanceOf(CustomRequestExecutor.class);
    }

    @Test
    void buildWithAnotherBuilder() {
        CustomBuilder builder = new CustomBuilder(new MetricRegistry(), anotherApacheBuilder);
        builder.build("test");
        assertThat(anotherApacheBuilder).extracting("requestExec")
                .isInstanceOf(CustomRequestExecutor.class);
    }

    @Test
    void configureCredentialReturnsNTCredentialsForNTLMConfig() {
        assertThat(builder.configureCredentials(new AuthConfiguration("username", "password", "NTLM", "realm", "hostname", "domain", "NT")))
                .isInstanceOfSatisfying(NTCredentials.class, credentials -> assertThat(credentials)
                        .satisfies(c -> assertThat(c.getPassword()).isEqualTo("password".toCharArray()))
                        .satisfies(c -> assertThat(c.getUserPrincipal().getName()).isEqualTo("DOMAIN\\username")));
    }

    @Test
    void configureCredentialReturnsUserNamePasswordCredentialsForBasicConfig() {
        assertThat(builder.configureCredentials(new AuthConfiguration("username", "password")))
                .isInstanceOfSatisfying(UsernamePasswordCredentials.class, upCredentials -> assertThat(upCredentials)
                        .satisfies(c -> assertThat(c.getPassword()).isEqualTo("password".toCharArray()))
                        .satisfies(c -> assertThat(c.getUserPrincipal().getName()).isEqualTo("username")));
    }
}
