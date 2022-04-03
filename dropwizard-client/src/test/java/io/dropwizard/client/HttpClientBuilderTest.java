package io.dropwizard.client;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.httpclient.HttpClientMetricNameStrategies;
import com.codahale.metrics.httpclient.InstrumentedHttpClientConnectionManager;
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
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.impl.conn.SystemDefaultRoutePlanner;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicListHeaderIterator;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpProcessor;
import org.apache.http.protocol.HttpRequestExecutor;
import org.assertj.core.api.InstanceOfAssertFactories;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import javax.annotation.Nullable;
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
import java.util.function.Function;

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

    private final Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create()
            .register("http", PlainConnectionSocketFactory.getSocketFactory())
            .register("https", SSLConnectionSocketFactory.getSocketFactory())
            .build();
    private HttpClientConfiguration configuration;
    private HttpClientBuilder builder;
    private InstrumentedHttpClientConnectionManager connectionManager;
    private org.apache.http.impl.client.HttpClientBuilder apacheBuilder;
    private AnotherHttpClientBuilder anotherApacheBuilder;

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

        assertThat(builder.using(customVerifier).createConfiguredRegistry())
            .isNotNull()
            .satisfies(configuredRegistry -> assertThat(configuredRegistry.lookup("https"))
                .isInstanceOf(SSLConnectionSocketFactory.class)
                .extracting("hostnameVerifier").isSameAs(customVerifier));
    }

    @Test
    void canUseACustomHostnameVerifierWhenTlsConfigurationSpecified() {
        final TlsConfiguration tlsConfiguration = new TlsConfiguration();
        tlsConfiguration.setVerifyHostname(true);
        configuration.setTlsConfiguration(tlsConfiguration);

        final HostnameVerifier customVerifier = (s, sslSession) -> false;

        assertThat(builder
                .using(configuration)
                .using(customVerifier)
                .createConfiguredRegistry())
            .isNotNull()
            .satisfies(configuredRegistry -> assertThat(configuredRegistry.lookup("https"))
                .isInstanceOf(SSLConnectionSocketFactory.class)
                .extracting("hostnameVerifier").isSameAs(customVerifier));
    }

    @Test
    void canUseASystemHostnameVerifierByDefaultWhenTlsConfigurationNotSpecified() {
        assertThat(builder.createConfiguredRegistry())
            .isNotNull()
            .satisfies(configuredRegistry -> assertThat(configuredRegistry.lookup("https"))
                .isInstanceOf(SSLConnectionSocketFactory.class)
                .extracting("hostnameVerifier").isInstanceOf(HostnameVerifier.class));
    }

    @Test
    void canUseASystemHostnameVerifierByDefaultWhenTlsConfigurationSpecified() {
        final TlsConfiguration tlsConfiguration = new TlsConfiguration();
        tlsConfiguration.setVerifyHostname(true);
        configuration.setTlsConfiguration(tlsConfiguration);

        assertThat(builder.using(configuration).createConfiguredRegistry())
            .isNotNull()
            .satisfies(configuredRegistry -> assertThat(configuredRegistry.lookup("https"))
                .isInstanceOf(SSLConnectionSocketFactory.class)
                .extracting("hostnameVerifier").isInstanceOf(HostnameVerifier.class));
    }

    @Test
    void createClientCanPassCustomVerifierToApacheBuilder() {
        final HostnameVerifier customVerifier = (s, sslSession) -> false;

        assertThat(builder.using(customVerifier).createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        assertThat(apacheBuilder)
            .extracting("hostnameVerifier")
            .isSameAs(customVerifier);
    }

    @Test
    void doesNotReuseConnectionsIfKeepAliveIsZero() {
        configuration.setKeepAlive(Duration.seconds(0));
        assertThat(builder.using(configuration).createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        assertThat(apacheBuilder).extracting("reuseStrategy")
                .isInstanceOf(NoConnectionReuseStrategy.class);
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
        assertThat(builder.using(configuration).createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        final HttpContext context = mock(HttpContext.class);
        final HttpResponse response = mock(HttpResponse.class);
        when(response.headerIterator(HTTP.CONN_KEEP_ALIVE)).thenReturn(mock(HeaderIterator.class));

        assertThat(apacheBuilder)
            .extracting("keepAliveStrategy")
            .isInstanceOfSatisfying(DefaultConnectionKeepAliveStrategy.class, strategy ->
                assertThat(strategy.getKeepAliveDuration(response, context)).isEqualTo(1_000));
    }

    @Test
    void usesDefaultForNonPersistentConnections() {
        configuration.setKeepAlive(Duration.seconds(1));
        assertThat(builder.using(configuration).createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        final HttpContext context = mock(HttpContext.class);
        final HttpResponse response = mock(HttpResponse.class);
        final HeaderIterator iterator = new BasicListHeaderIterator(
                Collections.singletonList(new BasicHeader(HttpHeaders.CONNECTION, "timeout=50")),
                HttpHeaders.CONNECTION
        );
        when(response.headerIterator(HTTP.CONN_KEEP_ALIVE)).thenReturn(iterator);

        assertThat(apacheBuilder)
            .extracting("keepAliveStrategy")
            .isInstanceOfSatisfying(DefaultConnectionKeepAliveStrategy.class, strategy ->
                assertThat(strategy.getKeepAliveDuration(response, context)).isEqualTo(50_000));
    }

    @Test
    void ignoresCookiesByDefault() {
        assertThat(builder.using(configuration).createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        assertThat(apacheBuilder)
            .extracting("defaultRequestConfig")
            .isInstanceOfSatisfying(RequestConfig.class, config ->
                assertThat(config.getCookieSpec()).isEqualTo(CookieSpecs.IGNORE_COOKIES));
    }

    @Test
    void usesBestMatchCookiePolicyIfCookiesAreEnabled() {
        configuration.setCookiesEnabled(true);
        assertThat(builder.using(configuration).createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        assertThat(apacheBuilder)
            .extracting("defaultRequestConfig")
            .isInstanceOfSatisfying(RequestConfig.class, config ->
                assertThat(config.getCookieSpec()).isEqualTo(CookieSpecs.DEFAULT));
    }

    @Test
    void normalizeUriByDefault() {
        assertThat(builder.using(configuration).createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        assertThat(apacheBuilder)
            .extracting("defaultRequestConfig")
            .isInstanceOfSatisfying(RequestConfig.class, config -> assertThat(config.isNormalizeUri()).isTrue());
    }

    @Test
    void disableNormalizeUriWhenDisabled() {
        configuration.setNormalizeUriEnabled(false);
        assertThat(builder.using(configuration).createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        assertThat(apacheBuilder)
            .extracting("defaultRequestConfig")
            .isInstanceOfSatisfying(RequestConfig.class, config -> assertThat(config.isNormalizeUri()).isFalse());
    }

    @Test
    void setsTheSocketTimeout() {
        configuration.setTimeout(Duration.milliseconds(500));
        assertThat(builder.using(configuration).createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        assertThat(apacheBuilder)
            .extracting("defaultRequestConfig")
            .isInstanceOfSatisfying(RequestConfig.class, config -> assertThat(config.getSocketTimeout()).isEqualTo(500));
    }

    @Test
    void setsTheConnectTimeout() {
        configuration.setConnectionTimeout(Duration.milliseconds(500));
        assertThat(builder.using(configuration).createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        assertThat(apacheBuilder)
            .extracting("defaultRequestConfig")
            .isInstanceOfSatisfying(RequestConfig.class, config -> assertThat(config.getConnectTimeout()).isEqualTo(500));
    }

    @Test
    void setsTheConnectionRequestTimeout() {
        configuration.setConnectionRequestTimeout(Duration.milliseconds(123));

        assertThat(builder.using(configuration).createClient(apacheBuilder, connectionManager, "test")).isNotNull();
        assertThat(apacheBuilder)
            .extracting("defaultRequestConfig")
            .isInstanceOfSatisfying(RequestConfig.class, config -> assertThat(config.getConnectionRequestTimeout()).isEqualTo(123));
    }

    @Test
    void disablesNaglesAlgorithm() {
        assertThat(builder.using(configuration).createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        assertThat(apacheBuilder)
            .extracting("defaultSocketConfig")
            .isInstanceOfSatisfying(SocketConfig.class, config -> assertThat(config.isTcpNoDelay()).isTrue());
    }

    @Test
    void disablesStaleConnectionCheck() {
        assertThat(builder.using(configuration).createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        // It is fine to use the isStaleConnectionCheckEnabled deprecated API, as we are ensuring
        // that the builder creates a client that does not check for stale connections on each
        // request, which adds significant overhead.
        assertThat(apacheBuilder)
            .extracting("defaultRequestConfig")
            .isInstanceOfSatisfying(RequestConfig.class, config -> assertThat(config.isStaleConnectionCheckEnabled()).isFalse());
    }

    @Test
    void usesTheDefaultRoutePlanner() {
        final CloseableHttpClient httpClient = builder.using(configuration)
                .createClient(apacheBuilder, connectionManager, "test").getClient();

        assertThat(httpClient)
            .isNotNull()
            .extracting("routePlanner").isInstanceOf(DefaultRoutePlanner.class);
        assertThat(apacheBuilder).extracting("routePlanner").isNull();
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

        assertThat(httpClient)
            .isNotNull()
            .extracting("routePlanner").isSameAs(routePlanner);
        assertThat(apacheBuilder).extracting("routePlanner").isSameAs(routePlanner);
    }

    @Test
    void usesACustomHttpRequestRetryHandler() {
        final HttpRequestRetryHandler customHandler = (exception, executionCount, context) -> false;

        configuration.setRetries(1);
        assertThat(builder.using(configuration).using(customHandler)
                .createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        assertThat(apacheBuilder).extracting("retryHandler").isSameAs(customHandler);
    }

    @Test
    void usesCredentialsProvider() {
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

        AuthScope authScope = new AuthScope("192.168.52.11", 8080);
        Credentials credentials = new UsernamePasswordCredentials("secret", "stuff");

        CloseableHttpClient httpClient = checkProxy(config, new HttpHost("dropwizard.io", 80),
                new HttpHost("192.168.52.11", 8080, "http"));

        assertThat(httpClient)
            .extracting("credentialsProvider")
            .isInstanceOfSatisfying(CredentialsProvider.class, credentialsProvider ->
                assertThat(credentialsProvider.getCredentials(authScope)).isEqualTo(credentials));
    }

    @Test
    void usesProxyWithNtlmAuth() {
        HttpClientConfiguration config = new HttpClientConfiguration();
        AuthConfiguration auth = new AuthConfiguration("secret", "stuff", "NTLM", "realm", "host", "domain", "NT");
        ProxyConfiguration proxy = new ProxyConfiguration("192.168.52.11", 8080, "http", auth);
        config.setProxyConfiguration(proxy);

        CloseableHttpClient httpClient = checkProxy(config, new HttpHost("dropwizard.io", 80),
                new HttpHost("192.168.52.11", 8080, "http"));

        AuthScope authScope = new AuthScope("192.168.52.11", 8080, "realm", "NTLM");
        Credentials credentials = new NTCredentials("secret", "stuff", "host", "domain");

        assertThat(httpClient)
            .extracting("credentialsProvider")
            .isInstanceOfSatisfying(CredentialsProvider.class, credentialsProvider ->
                assertThat(credentialsProvider.getCredentials(authScope)).isEqualTo(credentials));
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

        Function<HttpRoutePlanner, HttpRoute> safeDetermineRoute = planner -> {
            try {
                return planner.determineRoute(target, new HttpGet(target.toURI()), new BasicHttpContext());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };

        assertThat(httpClient)
            .extracting("routePlanner")
            .isInstanceOfSatisfying(HttpRoutePlanner.class, routePlanner -> assertThat(safeDetermineRoute.apply(routePlanner))
                .satisfies(route -> assertThat(route.getProxyHost()).isEqualTo(expectedProxy))
                .satisfies(route -> assertThat(route.getTargetHost()).isEqualTo(target))
                .satisfies(route -> assertThat(route.getHopCount()).isEqualTo(expectedProxy != null ? 2 : 1)));

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
        verify(connectionManager).setValidateAfterInactivity(validateAfterInactivityPeriod);
    }

    @Test
    void usesACustomHttpClientMetricNameStrategy() {
        assertThat(builder.using(HttpClientMetricNameStrategies.HOST_AND_METHOD)
                .createClient(apacheBuilder, connectionManager, "test"))
                .isNotNull();
        assertThat(apacheBuilder)
            .extracting("requestExec")
            .extracting("metricNameStrategy")
            .isSameAs(HttpClientMetricNameStrategies.HOST_AND_METHOD);
    }

    @Test
    void usesMethodOnlyHttpClientMetricNameStrategyByDefault() {
        assertThat(builder.createClient(apacheBuilder, connectionManager, "test"))
                .isNotNull();
        assertThat(apacheBuilder)
            .extracting("requestExec")
            .extracting("metricNameStrategy")
            .isSameAs(HttpClientMetricNameStrategies.METHOD_ONLY);
    }

    @Test
    void exposedConfigIsTheSameAsInternalToTheWrappedHttpClient() {
        assertThat(builder.createClient(apacheBuilder, connectionManager, "test"))
            .isNotNull()
            .satisfies(client -> assertThat(client.getClient())
                .extracting("defaultConfig")
                .isEqualTo(client.getDefaultRequestConfig()));
    }

    @Test
    void disablesContentCompression() {
        ConfiguredCloseableHttpClient client = builder
                .disableContentCompression(true)
                .createClient(apacheBuilder, connectionManager, "test");
        assertThat(client).isNotNull();

        assertThat(apacheBuilder)
            .extracting("contentCompressionDisabled", InstanceOfAssertFactories.BOOLEAN)
            .isTrue();
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
            public HttpUriRequest getRedirect(HttpRequest httpRequest,
                                              HttpResponse httpResponse,
                                              HttpContext httpContext) {
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
        assertThat(apacheBuilder).extracting("httpprocessor").isSameAs(httpProcessor);
    }

    @Test
    void usesServiceUnavailableRetryStrategy() {
        ServiceUnavailableRetryStrategy serviceUnavailableRetryStrategy = mock(ServiceUnavailableRetryStrategy.class);
        final ConfiguredCloseableHttpClient client =
            builder.using(serviceUnavailableRetryStrategy)
                .createClient(apacheBuilder, connectionManager, "test");
        assertThat(client).isNotNull();
        assertThat(apacheBuilder).extracting("serviceUnavailStrategy").isSameAs(serviceUnavailableRetryStrategy);
    }

    @Test
    void allowsCustomBuilderConfiguration() {
        CustomBuilder builder = new CustomBuilder(new MetricRegistry());
        assertThat(builder.customized).isFalse();
        builder.createClient(apacheBuilder, connectionManager, "test");
        assertThat(builder.customized).isTrue();
        assertThat(apacheBuilder).extracting("requestExec").isInstanceOf(CustomRequestExecutor.class);
    }

    @Test
    void buildWithAnotherBuilder() {
        new CustomBuilder(new MetricRegistry(), anotherApacheBuilder).build("test");
        assertThat(anotherApacheBuilder).extracting("requestExec").isInstanceOf(CustomRequestExecutor.class);
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
}
