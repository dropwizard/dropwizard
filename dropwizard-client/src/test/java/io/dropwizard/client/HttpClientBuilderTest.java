package io.dropwizard.client;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.httpclient.HttpClientMetricNameStrategies;
import com.codahale.metrics.httpclient.InstrumentedHttpClientConnectionManager;
import com.codahale.metrics.httpclient.InstrumentedHttpRequestExecutor;
import com.google.common.collect.ImmutableList;
import io.dropwizard.client.proxy.AuthConfiguration;
import io.dropwizard.client.proxy.ProxyConfiguration;
import io.dropwizard.lifecycle.Managed;
import io.dropwizard.lifecycle.setup.LifecycleEnvironment;
import io.dropwizard.setup.Environment;
import io.dropwizard.util.Duration;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpHost;
import org.apache.http.HttpRequest;
import org.apache.http.HttpResponse;
import org.apache.http.ProtocolException;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.RedirectStrategy;
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
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class HttpClientBuilderTest {
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

    public HttpClientBuilderTest() throws ClassNotFoundException {
        this.httpClientBuilderClass = Class.forName("org.apache.http.impl.client.HttpClientBuilder");
        this.httpClientClass = Class.forName("org.apache.http.impl.client.InternalHttpClient");
    }

    @Before
    public void setUp() {
        final MetricRegistry metricRegistry = new MetricRegistry();
        configuration = new HttpClientConfiguration();
        builder = new HttpClientBuilder(metricRegistry);
        connectionManager = spy(new InstrumentedHttpClientConnectionManager(metricRegistry, registry));
        apacheBuilder = org.apache.http.impl.client.HttpClientBuilder.create();

        initMocks(this);
    }

    @Test
    public void setsTheMaximumConnectionPoolSize() throws Exception {
        configuration.setMaxConnections(412);
        final ConfiguredCloseableHttpClient client = builder.using(configuration)
                .createClient(apacheBuilder, builder.configureConnectionManager(connectionManager), "test");

        assertThat(client).isNotNull();
        assertThat(spyHttpClientBuilderField("connManager", apacheBuilder)).isSameAs(connectionManager);
        verify(connectionManager).setMaxTotal(412);
    }


    @Test
    public void setsTheMaximumRoutePoolSize() throws Exception {
        configuration.setMaxConnectionsPerRoute(413);
        final ConfiguredCloseableHttpClient client = builder.using(configuration)
                .createClient(apacheBuilder, builder.configureConnectionManager(connectionManager), "test");

        assertThat(client).isNotNull();
        assertThat(spyHttpClientBuilderField("connManager", apacheBuilder)).isSameAs(connectionManager);
        verify(connectionManager).setDefaultMaxPerRoute(413);
    }

    @Test
    public void setsTheUserAgent() throws Exception {
        configuration.setUserAgent(Optional.of("qwerty"));
        assertThat(builder.using(configuration).createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        assertThat(spyHttpClientBuilderField("userAgent", apacheBuilder)).isEqualTo("qwerty");
    }

    @Test
    public void canUseACustomDnsResolver() throws Exception {
        final DnsResolver resolver = mock(DnsResolver.class);
        final InstrumentedHttpClientConnectionManager manager =
                builder.using(resolver).createConnectionManager(registry, "test");

        // Yes, this is gross. Thanks, Apache!
        final Field connectionOperatorField =
                FieldUtils.getField(PoolingHttpClientConnectionManager.class, "connectionOperator", true);
        final Object connectOperator = connectionOperatorField.get(manager);
        final Field dnsResolverField = FieldUtils.getField(connectOperator.getClass(), "dnsResolver", true);
        assertThat(dnsResolverField.get(connectOperator)).isEqualTo(resolver);
    }


    @Test
    public void usesASystemDnsResolverByDefault() throws Exception {
        final InstrumentedHttpClientConnectionManager manager = builder.createConnectionManager(registry, "test");

        // Yes, this is gross. Thanks, Apache!
        final Field connectionOperatorField =
                FieldUtils.getField(PoolingHttpClientConnectionManager.class, "connectionOperator", true);
        final Object connectOperator = connectionOperatorField.get(manager);
        final Field dnsResolverField = FieldUtils.getField(connectOperator.getClass(), "dnsResolver", true);
        assertThat(dnsResolverField.get(connectOperator)).isInstanceOf(SystemDefaultDnsResolver.class);
    }


    @Test
    public void doesNotReuseConnectionsIfKeepAliveIsZero() throws Exception {
        configuration.setKeepAlive(Duration.seconds(0));
        assertThat(builder.using(configuration).createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        assertThat(spyHttpClientBuilderField("reuseStrategy", apacheBuilder))
                .isInstanceOf(NoConnectionReuseStrategy.class);
    }


    @Test
    public void reusesConnectionsIfKeepAliveIsNonZero() throws Exception {
        configuration.setKeepAlive(Duration.seconds(1));
        assertThat(builder.using(configuration).createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        assertThat(spyHttpClientBuilderField("reuseStrategy", apacheBuilder))
                .isInstanceOf(DefaultConnectionReuseStrategy.class);
    }

    @Test
    public void usesKeepAliveForPersistentConnections() throws Exception {
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
    public void usesDefaultForNonPersistentConnections() throws Exception {
        configuration.setKeepAlive(Duration.seconds(1));
        assertThat(builder.using(configuration).createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        final Field field = FieldUtils.getField(httpClientBuilderClass, "keepAliveStrategy", true);
        final DefaultConnectionKeepAliveStrategy strategy = (DefaultConnectionKeepAliveStrategy) field.get(apacheBuilder);
        final HttpContext context = mock(HttpContext.class);
        final HttpResponse response = mock(HttpResponse.class);
        final HeaderIterator iterator = new BasicListHeaderIterator(
                ImmutableList.<Header>of(new BasicHeader(HttpHeaders.CONNECTION, "timeout=50")),
                HttpHeaders.CONNECTION
        );
        when(response.headerIterator(HTTP.CONN_KEEP_ALIVE)).thenReturn(iterator);

        assertThat(strategy.getKeepAliveDuration(response, context)).isEqualTo(50000);
    }

    @Test
    public void ignoresCookiesByDefault() throws Exception {
        assertThat(builder.using(configuration).createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        assertThat(((RequestConfig) spyHttpClientBuilderField("defaultRequestConfig", apacheBuilder)).getCookieSpec())
                .isEqualTo(CookieSpecs.IGNORE_COOKIES);
    }

    @Test
    public void usesBestMatchCookiePolicyIfCookiesAreEnabled() throws Exception {
        configuration.setCookiesEnabled(true);
        assertThat(builder.using(configuration).createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        assertThat(((RequestConfig) spyHttpClientBuilderField("defaultRequestConfig", apacheBuilder)).getCookieSpec())
                .isEqualTo(CookieSpecs.DEFAULT);
    }

    @Test
    public void setsTheSocketTimeout() throws Exception {
        configuration.setTimeout(Duration.milliseconds(500));
        assertThat(builder.using(configuration).createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        assertThat(((RequestConfig) spyHttpClientBuilderField("defaultRequestConfig", apacheBuilder)).getSocketTimeout())
                .isEqualTo(500);
    }

    @Test
    public void setsTheConnectTimeout() throws Exception {
        configuration.setConnectionTimeout(Duration.milliseconds(500));
        assertThat(builder.using(configuration).createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        assertThat(((RequestConfig) spyHttpClientBuilderField("defaultRequestConfig", apacheBuilder)).getConnectTimeout())
                .isEqualTo(500);
    }

    @Test
    public void setsTheConnectionRequestTimeout() throws Exception {
        configuration.setConnectionRequestTimeout(Duration.milliseconds(123));

        assertThat(builder.using(configuration).createClient(apacheBuilder, connectionManager, "test")).isNotNull();
        assertThat(((RequestConfig) spyHttpClientBuilderField("defaultRequestConfig", apacheBuilder)).getConnectionRequestTimeout())
                .isEqualTo(123);
    }

    @Test
    public void disablesNaglesAlgorithm() throws Exception {
        assertThat(builder.using(configuration).createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        assertThat(((SocketConfig) spyHttpClientBuilderField("defaultSocketConfig", apacheBuilder)).isTcpNoDelay()).isTrue();
    }

    @Test
    public void disablesStaleConnectionCheck() throws Exception {
        assertThat(builder.using(configuration).createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        assertThat(((RequestConfig) spyHttpClientBuilderField("defaultRequestConfig", apacheBuilder))
                .isStaleConnectionCheckEnabled()).isFalse();
    }

    @Test
    public void usesTheDefaultRoutePlanner() throws Exception {
        final CloseableHttpClient httpClient = builder.using(configuration)
                .createClient(apacheBuilder, connectionManager, "test").getClient();

        assertThat(httpClient).isNotNull();
        assertThat(spyHttpClientBuilderField("routePlanner", apacheBuilder)).isNull();
        assertThat(spyHttpClientField("routePlanner", httpClient)).isInstanceOf(DefaultRoutePlanner.class);
    }

    @Test
    public void usesACustomRoutePlanner() throws Exception {
        final HttpRoutePlanner routePlanner = new SystemDefaultRoutePlanner(new ProxySelector() {
            @Override
            public List<Proxy> select(URI uri) {
                return ImmutableList.of(new Proxy(Proxy.Type.HTTP, new InetSocketAddress("192.168.52.1", 8080)));
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
    public void usesACustomHttpRequestRetryHandler() throws Exception {
        final HttpRequestRetryHandler customHandler = (exception, executionCount, context) -> false;

        configuration.setRetries(1);
        assertThat(builder.using(configuration).using(customHandler)
                .createClient(apacheBuilder, connectionManager, "test")).isNotNull();

        assertThat(spyHttpClientBuilderField("retryHandler", apacheBuilder)).isSameAs(customHandler);
    }

    @Test
    public void usesCredentialsProvider() throws Exception {
        final CredentialsProvider credentialsProvider = new CredentialsProvider() {
            @Override
            public void setCredentials(AuthScope authscope, Credentials credentials) {
            }

            @Override
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
    public void usesProxy() throws Exception {
        HttpClientConfiguration config = new HttpClientConfiguration();
        ProxyConfiguration proxy = new ProxyConfiguration("192.168.52.11", 8080);
        config.setProxyConfiguration(proxy);

        checkProxy(config, new HttpHost("dropwizard.io", 80), new HttpHost("192.168.52.11", 8080));
    }

    @Test
    public void usesProxyWithoutPort() throws Exception {
        HttpClientConfiguration config = new HttpClientConfiguration();
        ProxyConfiguration proxy = new ProxyConfiguration("192.168.52.11");
        config.setProxyConfiguration(proxy);

        checkProxy(config, new HttpHost("dropwizard.io", 80), new HttpHost("192.168.52.11"));
    }

    @Test
    public void usesProxyWithAuth() throws Exception {
        HttpClientConfiguration config = new HttpClientConfiguration();
        AuthConfiguration auth = new AuthConfiguration("secret", "stuff");
        ProxyConfiguration proxy = new ProxyConfiguration("192.168.52.11", 8080, "http", auth);
        config.setProxyConfiguration(proxy);

        CloseableHttpClient httpClient = checkProxy(config, new HttpHost("dropwizard.io", 80),
                new HttpHost("192.168.52.11", 8080, "http"));
        CredentialsProvider credentialsProvider = (CredentialsProvider)
                FieldUtils.getField(httpClient.getClass(), "credentialsProvider", true)
                        .get(httpClient);

        assertThat(credentialsProvider.getCredentials(new AuthScope("192.168.52.11", 8080)))
                .isEqualTo(new UsernamePasswordCredentials("secret", "stuff"));
    }

    @Test
    public void usesProxyWithNonProxyHosts() throws Exception {
        HttpClientConfiguration config = new HttpClientConfiguration();
        ProxyConfiguration proxy = new ProxyConfiguration("192.168.52.11", 8080);
        proxy.setNonProxyHosts(ImmutableList.of("*.example.com"));
        config.setProxyConfiguration(proxy);

        checkProxy(config, new HttpHost("host.example.com", 80), null);
    }

    @Test
    public void usesProxyWithNonProxyHostsAndTargetDoesNotMatch() throws Exception {
        HttpClientConfiguration config = new HttpClientConfiguration();
        ProxyConfiguration proxy = new ProxyConfiguration("192.168.52.11");
        proxy.setNonProxyHosts(ImmutableList.of("*.example.com"));
        config.setProxyConfiguration(proxy);

        checkProxy(config, new HttpHost("dropwizard.io", 80), new HttpHost("192.168.52.11"));
    }

    @Test
    public void usesNoProxy() throws Exception {
        checkProxy(new HttpClientConfiguration(), new HttpHost("dropwizard.io", 80), null);
    }

    private CloseableHttpClient checkProxy(HttpClientConfiguration config, HttpHost target, HttpHost expectedProxy)
            throws Exception {
        CloseableHttpClient httpClient = builder.using(config).build("test");
        HttpRoutePlanner routePlanner = (HttpRoutePlanner)
                FieldUtils.getField(httpClient.getClass(), "routePlanner", true).get(httpClient);

        HttpRoute route = routePlanner.determineRoute(target, new HttpGet(target.toURI()),
                new BasicHttpContext());
        assertThat(route.getProxyHost()).isEqualTo(expectedProxy);
        assertThat(route.getTargetHost()).isEqualTo(target);
        assertThat(route.getHopCount()).isEqualTo(expectedProxy != null ? 2 : 1);

        return httpClient;
    }

    @Test
    public void setValidateAfterInactivityPeriodFromConfiguration() throws Exception {
        int validateAfterInactivityPeriod = 50000;
        configuration.setValidateAfterInactivityPeriod(Duration.milliseconds(validateAfterInactivityPeriod));
        final ConfiguredCloseableHttpClient client = builder.using(configuration)
                .createClient(apacheBuilder, builder.configureConnectionManager(connectionManager), "test");

        assertThat(client).isNotNull();
        assertThat(spyHttpClientBuilderField("connManager", apacheBuilder)).isSameAs(connectionManager);
        verify(connectionManager).setValidateAfterInactivity(validateAfterInactivityPeriod);
    }

    @Test
    public void usesACustomHttpClientMetricNameStrategy() throws Exception {
        assertThat(builder.using(HttpClientMetricNameStrategies.HOST_AND_METHOD)
                .createClient(apacheBuilder, connectionManager, "test"))
                .isNotNull();
        assertThat(FieldUtils.getField(InstrumentedHttpRequestExecutor.class,
                "metricNameStrategy", true)
                .get(spyHttpClientBuilderField("requestExec", apacheBuilder)))
                .isSameAs(HttpClientMetricNameStrategies.HOST_AND_METHOD);
    }

    @Test
    public void usesMethodOnlyHttpClientMetricNameStrategyByDefault() throws Exception {
        assertThat(builder.createClient(apacheBuilder, connectionManager, "test"))
                .isNotNull();
        assertThat(FieldUtils.getField(InstrumentedHttpRequestExecutor.class,
                "metricNameStrategy", true)
                .get(spyHttpClientBuilderField("requestExec", apacheBuilder)))
                .isSameAs(HttpClientMetricNameStrategies.METHOD_ONLY);
    }

    @Test
    public void exposedConfigIsTheSameAsInternalToTheWrappedHttpClient() throws Exception {
        ConfiguredCloseableHttpClient client = builder.createClient(apacheBuilder, connectionManager, "test");
        assertThat(client).isNotNull();

        assertThat(spyHttpClientField("defaultConfig", client.getClient())).isEqualTo(client.getDefaultRequestConfig());
    }

    @Test
    public void disablesContentCompression() throws Exception {
        ConfiguredCloseableHttpClient client = builder
                .disableContentCompression(true)
                .createClient(apacheBuilder, connectionManager, "test");
        assertThat(client).isNotNull();

        final Boolean contentCompressionDisabled = (Boolean) FieldUtils
                .getField(httpClientBuilderClass, "contentCompressionDisabled", true)
                .get(apacheBuilder);
        assertThat(contentCompressionDisabled).isTrue();
    }

    @Test
    public void managedByEnvironment() throws Exception {
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
    public void usesACustomRedirectStrategy() throws Exception {
        RedirectStrategy neverFollowRedirectStrategy = new RedirectStrategy() {
            @Override
            public boolean isRedirected(HttpRequest httpRequest,
                                        HttpResponse httpResponse,
                                        HttpContext httpContext) throws ProtocolException {
                return false;
            }

            @Override
            public HttpUriRequest getRedirect(HttpRequest httpRequest,
                                              HttpResponse httpResponse,
                                              HttpContext httpContext) throws ProtocolException {
                return null;
            }
        };
        ConfiguredCloseableHttpClient client = builder.using(neverFollowRedirectStrategy)
                                                      .createClient(apacheBuilder, connectionManager, "test");
        assertThat(client).isNotNull();
        assertThat(spyHttpClientBuilderField("redirectStrategy", apacheBuilder)).isSameAs(neverFollowRedirectStrategy);
    }

    @Test
    public void usesDefaultHeaders() throws Exception {
        final ConfiguredCloseableHttpClient client =
                builder.using(ImmutableList.of(new BasicHeader(HttpHeaders.ACCEPT_LANGUAGE, "de")))
                        .createClient(apacheBuilder, connectionManager, "test");
        assertThat(client).isNotNull();

        @SuppressWarnings("unchecked")
        List<? extends Header> defaultHeaders = (List<? extends Header>) FieldUtils
                .getField(httpClientBuilderClass, "defaultHeaders", true)
                .get(apacheBuilder);

        assertThat(defaultHeaders).hasSize(1);
        final Header header = defaultHeaders.get(0);
        assertThat(header.getName()).isEqualTo(HttpHeaders.ACCEPT_LANGUAGE);
        assertThat(header.getValue()).isEqualTo("de");
    }

    private Object spyHttpClientBuilderField(final String fieldName, final Object obj) throws Exception {
        final Field field = FieldUtils.getField(httpClientBuilderClass, fieldName, true);
        return field.get(obj);
    }

    private Object spyHttpClientField(final String fieldName, final Object obj) throws Exception {
        final Field field = FieldUtils.getField(httpClientClass, fieldName, true);
        return field.get(obj);
    }
}
