package io.dropwizard.client;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.httpclient.InstrumentedHttpClientConnectionManager;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import io.dropwizard.util.Duration;
import java.io.IOException;
import java.lang.reflect.Field;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.CookieSpecs;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.config.SocketConfig;
import org.apache.http.conn.ConnectionKeepAliveStrategy;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.HttpClientConnectionManager;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicListHeaderIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.apache.http.protocol.HttpRequestExecutor;
import static org.assertj.core.api.Assertions.assertThat;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@PowerMockIgnore( {"org.apache.http.conn.ssl.*", "com.sun.org.apache.xerces.*", 
    "javax.xml.parsers.*", "ch.qos.logback.*", "org.slf4j.*"})
@PrepareForTest({HttpClientBuilder.class,
                 org.apache.http.impl.client.HttpClientBuilder.class})
@RunWith(PowerMockRunner.class)

public class HttpClientBuilderTest {
    private final HttpClientConfiguration configuration = new HttpClientConfiguration();
    private final DnsResolver resolver = mock(DnsResolver.class);
    private final HttpClientBuilder builder = new HttpClientBuilder(new MetricRegistry());
    private final Registry<ConnectionSocketFactory> registry = 
            RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", SSLConnectionSocketFactory.getSocketFactory())
                .build();

    @Test
    public void setsTheMaximumConnectionPoolSize() throws Exception {
        configuration.setMaxConnections(412);
        final InstrumentedHttpClientConnectionManager manager = mock(InstrumentedHttpClientConnectionManager.class);
        PowerMockito.whenNew(InstrumentedHttpClientConnectionManager.class).withAnyArguments().thenReturn(manager);
        final org.apache.http.impl.client.HttpClientBuilder apacheBuilder = PowerMockito.mock(org.apache.http.impl.client.HttpClientBuilder.class);
        final CloseableHttpClient client = mock(CloseableHttpClient.class);
        
        PowerMockito.mockStatic(org.apache.http.impl.client.HttpClientBuilder.class);
        when(org.apache.http.impl.client.HttpClientBuilder.create()).thenReturn(apacheBuilder);
        when(apacheBuilder.setRequestExecutor(any(HttpRequestExecutor.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setConnectionManager(same(manager))).thenReturn(apacheBuilder).thenReturn(apacheBuilder);
        when(apacheBuilder.setDefaultRequestConfig(any(RequestConfig.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setDefaultSocketConfig(any(SocketConfig.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setConnectionReuseStrategy(any(ConnectionReuseStrategy.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setRetryHandler(any(HttpRequestRetryHandler.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setUserAgent(any(String.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.build()).thenReturn(client);

        final CloseableHttpClient returnClient = builder.using(configuration).build("test");
        verify(manager).setMaxTotal(412);
        verify(apacheBuilder).setConnectionManager(manager);
        assertThat(returnClient == client).isTrue();
    }

    
    @Test
    public void setsTheMaximumRoutePoolSize() throws Exception {
        configuration.setMaxConnectionsPerRoute(413);
        final InstrumentedHttpClientConnectionManager manager = mock(InstrumentedHttpClientConnectionManager.class);
        PowerMockito.whenNew(InstrumentedHttpClientConnectionManager.class).withAnyArguments().thenReturn(manager);
        final org.apache.http.impl.client.HttpClientBuilder apacheBuilder = PowerMockito.mock(org.apache.http.impl.client.HttpClientBuilder.class);
        final CloseableHttpClient client = mock(CloseableHttpClient.class);
        
        PowerMockito.mockStatic(org.apache.http.impl.client.HttpClientBuilder.class);
        when(org.apache.http.impl.client.HttpClientBuilder.create()).thenReturn(apacheBuilder);
        when(apacheBuilder.setRequestExecutor(any(HttpRequestExecutor.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setConnectionManager(same(manager))).thenReturn(apacheBuilder).thenReturn(apacheBuilder);
        when(apacheBuilder.setDefaultRequestConfig(any(RequestConfig.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setDefaultSocketConfig(any(SocketConfig.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setConnectionReuseStrategy(any(ConnectionReuseStrategy.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setRetryHandler(any(HttpRequestRetryHandler.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setUserAgent(any(String.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.build()).thenReturn(client);

        final CloseableHttpClient returnClient = builder.using(configuration).build("test"); 
        verify(manager).setDefaultMaxPerRoute(413);
        verify(apacheBuilder).setConnectionManager(manager);
        assertThat(returnClient == client).isTrue();
    }

    @Test
    public void setsTheUserAgent() {
        configuration.setUserAgent(Optional.of("qwerty"));
        
        final org.apache.http.impl.client.HttpClientBuilder apacheBuilder = PowerMockito.mock(org.apache.http.impl.client.HttpClientBuilder.class);
        final CloseableHttpClient client = mock(CloseableHttpClient.class);
        
        PowerMockito.mockStatic(org.apache.http.impl.client.HttpClientBuilder.class);
        when(org.apache.http.impl.client.HttpClientBuilder.create()).thenReturn(apacheBuilder);
        when(apacheBuilder.setRequestExecutor(any(HttpRequestExecutor.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setConnectionManager(any(HttpClientConnectionManager.class))).thenReturn(apacheBuilder).thenReturn(apacheBuilder);
        when(apacheBuilder.setDefaultRequestConfig(any(RequestConfig.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setDefaultSocketConfig(any(SocketConfig.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setConnectionReuseStrategy(any(ConnectionReuseStrategy.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setRetryHandler(any(HttpRequestRetryHandler.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setKeepAliveStrategy(any(ConnectionKeepAliveStrategy.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setUserAgent(any(String.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.build()).thenReturn(client);
        
        final CloseableHttpClient returnClient = builder.using(configuration).build("test"); 
        final ArgumentCaptor<String> argument = ArgumentCaptor.forClass(String.class);
        verify(apacheBuilder).setUserAgent(argument.capture());

        assertThat(argument.getValue())
                .isEqualTo("qwerty");
    }

    @Test
    public void canUseACustomDnsResolver() throws Exception {
        final org.apache.http.impl.client.HttpClientBuilder apacheBuilder = PowerMockito.mock(org.apache.http.impl.client.HttpClientBuilder.class);
        final CloseableHttpClient client = mock(CloseableHttpClient.class);
        
        PowerMockito.mockStatic(org.apache.http.impl.client.HttpClientBuilder.class);
        when(org.apache.http.impl.client.HttpClientBuilder.create()).thenReturn(apacheBuilder);
        when(apacheBuilder.setRequestExecutor(any(HttpRequestExecutor.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setConnectionManager(any(HttpClientConnectionManager.class))).thenReturn(apacheBuilder).thenReturn(apacheBuilder);
        when(apacheBuilder.setDefaultRequestConfig(any(RequestConfig.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setDefaultSocketConfig(any(SocketConfig.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setConnectionReuseStrategy(any(ConnectionReuseStrategy.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setRetryHandler(any(HttpRequestRetryHandler.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setUserAgent(any(String.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.build()).thenReturn(client);
        
        final CloseableHttpClient returnClient = builder.using(resolver).build("test"); 
        final ArgumentCaptor<HttpClientConnectionManager> argument = ArgumentCaptor.forClass(HttpClientConnectionManager.class);
        verify(apacheBuilder).setConnectionManager(argument.capture());
        
        // Yes, this is gross. Thanks, Apache!
        Field field = PoolingHttpClientConnectionManager.class.getDeclaredField("connectionOperator");
        field.setAccessible(true);
        final Object connectOperator = field.get(argument.getValue());
        field = connectOperator.getClass().getDeclaredField("dnsResolver");
        field.setAccessible(true);
        assertThat(field.get(connectOperator))
                .isEqualTo(resolver);
    }

    
    @Test
    public void usesASystemDnsResolverByDefault() throws Exception {
        final org.apache.http.impl.client.HttpClientBuilder apacheBuilder = PowerMockito.mock(org.apache.http.impl.client.HttpClientBuilder.class);
        final CloseableHttpClient client = mock(CloseableHttpClient.class);
        
        PowerMockito.mockStatic(org.apache.http.impl.client.HttpClientBuilder.class);
        when(org.apache.http.impl.client.HttpClientBuilder.create()).thenReturn(apacheBuilder);
        when(apacheBuilder.setRequestExecutor(any(HttpRequestExecutor.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setConnectionManager(any(HttpClientConnectionManager.class))).thenReturn(apacheBuilder).thenReturn(apacheBuilder);
        when(apacheBuilder.setDefaultRequestConfig(any(RequestConfig.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setDefaultSocketConfig(any(SocketConfig.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setConnectionReuseStrategy(any(ConnectionReuseStrategy.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setRetryHandler(any(HttpRequestRetryHandler.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setUserAgent(any(String.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.build()).thenReturn(client);
        
        final CloseableHttpClient returnClient = builder.build("test"); 
        final ArgumentCaptor<HttpClientConnectionManager> argument = ArgumentCaptor.forClass(HttpClientConnectionManager.class);
        verify(apacheBuilder).setConnectionManager(argument.capture());
        
        // Yes, this is gross. Thanks, Apache!
        Field field = PoolingHttpClientConnectionManager.class.getDeclaredField("connectionOperator");
        field.setAccessible(true);
        final Object connectOperator = field.get(argument.getValue());
        field = connectOperator.getClass().getDeclaredField("dnsResolver");
        field.setAccessible(true);
        assertThat(field.get(connectOperator))
                .isInstanceOf(SystemDefaultDnsResolver.class);
    }

    
    @Test
    public void doesNotReuseConnectionsIfKeepAliveIsZero() throws Exception {
        configuration.setKeepAlive(Duration.seconds(0));
        
        final org.apache.http.impl.client.HttpClientBuilder apacheBuilder = PowerMockito.mock(org.apache.http.impl.client.HttpClientBuilder.class);
        final CloseableHttpClient client = mock(CloseableHttpClient.class);
        
        PowerMockito.mockStatic(org.apache.http.impl.client.HttpClientBuilder.class);
        when(org.apache.http.impl.client.HttpClientBuilder.create()).thenReturn(apacheBuilder);
        when(apacheBuilder.setRequestExecutor(any(HttpRequestExecutor.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setConnectionManager(any(HttpClientConnectionManager.class))).thenReturn(apacheBuilder).thenReturn(apacheBuilder);
        when(apacheBuilder.setDefaultRequestConfig(any(RequestConfig.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setDefaultSocketConfig(any(SocketConfig.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setConnectionReuseStrategy(any(ConnectionReuseStrategy.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setRetryHandler(any(HttpRequestRetryHandler.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setUserAgent(any(String.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.build()).thenReturn(client);
        
        final CloseableHttpClient returnClient = builder.using(configuration).build("test"); 
        final ArgumentCaptor<ConnectionReuseStrategy> argument = ArgumentCaptor.forClass(ConnectionReuseStrategy.class);
        verify(apacheBuilder).setConnectionReuseStrategy(argument.capture());
        assertThat(argument.getValue())
                .isInstanceOf(NoConnectionReuseStrategy.class);
    }

    
    @Test
    public void reusesConnectionsIfKeepAliveIsNonZero() throws Exception {
        configuration.setKeepAlive(Duration.seconds(1));
        
        final org.apache.http.impl.client.HttpClientBuilder apacheBuilder = PowerMockito.mock(org.apache.http.impl.client.HttpClientBuilder.class);
        final CloseableHttpClient client = mock(CloseableHttpClient.class);
        
        PowerMockito.mockStatic(org.apache.http.impl.client.HttpClientBuilder.class);
        when(org.apache.http.impl.client.HttpClientBuilder.create()).thenReturn(apacheBuilder);
        when(apacheBuilder.setRequestExecutor(any(HttpRequestExecutor.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setConnectionManager(any(HttpClientConnectionManager.class))).thenReturn(apacheBuilder).thenReturn(apacheBuilder);
        when(apacheBuilder.setDefaultRequestConfig(any(RequestConfig.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setDefaultSocketConfig(any(SocketConfig.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setConnectionReuseStrategy(any(ConnectionReuseStrategy.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setRetryHandler(any(HttpRequestRetryHandler.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setKeepAliveStrategy(any(ConnectionKeepAliveStrategy.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setUserAgent(any(String.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.build()).thenReturn(client);
        
        final CloseableHttpClient returnClient = builder.using(configuration).build("test"); 
        final ArgumentCaptor<ConnectionReuseStrategy> argument = ArgumentCaptor.forClass(ConnectionReuseStrategy.class);
        verify(apacheBuilder).setConnectionReuseStrategy(argument.capture());
        assertThat(argument.getValue())
                .isInstanceOf(DefaultConnectionReuseStrategy.class);
    }

    
    @Test
    public void usesKeepAliveForPersistentConnections() throws Exception {
        configuration.setKeepAlive(Duration.seconds(1));
        
        final org.apache.http.impl.client.HttpClientBuilder apacheBuilder = PowerMockito.mock(org.apache.http.impl.client.HttpClientBuilder.class);
        final CloseableHttpClient client = mock(CloseableHttpClient.class);
        
        PowerMockito.mockStatic(org.apache.http.impl.client.HttpClientBuilder.class);
        when(org.apache.http.impl.client.HttpClientBuilder.create()).thenReturn(apacheBuilder);
        when(apacheBuilder.setRequestExecutor(any(HttpRequestExecutor.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setConnectionManager(any(HttpClientConnectionManager.class))).thenReturn(apacheBuilder).thenReturn(apacheBuilder);
        when(apacheBuilder.setDefaultRequestConfig(any(RequestConfig.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setDefaultSocketConfig(any(SocketConfig.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setConnectionReuseStrategy(any(ConnectionReuseStrategy.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setRetryHandler(any(HttpRequestRetryHandler.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setKeepAliveStrategy(any(ConnectionKeepAliveStrategy.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setUserAgent(any(String.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.build()).thenReturn(client);
        
        final CloseableHttpClient returnClient = builder.using(configuration).build("test"); 
        final ArgumentCaptor<ConnectionKeepAliveStrategy> argument = ArgumentCaptor.forClass(ConnectionKeepAliveStrategy.class);
        verify(apacheBuilder).setKeepAliveStrategy(argument.capture());
        
        final DefaultConnectionKeepAliveStrategy strategy = (DefaultConnectionKeepAliveStrategy) 
                argument.getValue();
        
        final HttpResponse response = mock(HttpResponse.class);
        when(response.headerIterator(HTTP.CONN_KEEP_ALIVE)).thenReturn(mock(HeaderIterator.class));

        final HttpContext context = mock(HttpContext.class);

        assertThat(strategy.getKeepAliveDuration(response, context))
                .isEqualTo(1000);
        
    }

    
    @Test
    public void usesDefaultForNonPersistentConnections() throws Exception {
        configuration.setKeepAlive(Duration.seconds(1));
        
        final org.apache.http.impl.client.HttpClientBuilder apacheBuilder = PowerMockito.mock(org.apache.http.impl.client.HttpClientBuilder.class);
        final CloseableHttpClient client = mock(CloseableHttpClient.class);
        
        PowerMockito.mockStatic(org.apache.http.impl.client.HttpClientBuilder.class);
        when(org.apache.http.impl.client.HttpClientBuilder.create()).thenReturn(apacheBuilder);
        when(apacheBuilder.setRequestExecutor(any(HttpRequestExecutor.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setConnectionManager(any(HttpClientConnectionManager.class))).thenReturn(apacheBuilder).thenReturn(apacheBuilder);
        when(apacheBuilder.setDefaultRequestConfig(any(RequestConfig.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setDefaultSocketConfig(any(SocketConfig.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setConnectionReuseStrategy(any(ConnectionReuseStrategy.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setRetryHandler(any(HttpRequestRetryHandler.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setKeepAliveStrategy(any(ConnectionKeepAliveStrategy.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setUserAgent(any(String.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.build()).thenReturn(client);
        
        final CloseableHttpClient returnClient = builder.using(configuration).build("test"); 
        final ArgumentCaptor<ConnectionKeepAliveStrategy> argument = ArgumentCaptor.forClass(ConnectionKeepAliveStrategy.class);
        verify(apacheBuilder).setKeepAliveStrategy(argument.capture());
        
        final DefaultConnectionKeepAliveStrategy strategy = (DefaultConnectionKeepAliveStrategy) 
                argument.getValue();

        final HttpResponse response = mock(HttpResponse.class);

        final HeaderIterator iterator = new BasicListHeaderIterator(
                ImmutableList.<Header>of(new BasicHeader(HttpHeaders.CONNECTION, "timeout=50")),
                HttpHeaders.CONNECTION
        );

        when(response.headerIterator(HTTP.CONN_KEEP_ALIVE)).thenReturn(iterator);

        final HttpContext context = mock(HttpContext.class);

        assertThat(strategy.getKeepAliveDuration(response, context))
                .isEqualTo(50000);
    }

    
    @Test
    public void ignoresCookiesByDefault() throws Exception {        
        final org.apache.http.impl.client.HttpClientBuilder apacheBuilder = PowerMockito.mock(org.apache.http.impl.client.HttpClientBuilder.class);
        final CloseableHttpClient client = mock(CloseableHttpClient.class);
        
        PowerMockito.mockStatic(org.apache.http.impl.client.HttpClientBuilder.class);
        when(org.apache.http.impl.client.HttpClientBuilder.create()).thenReturn(apacheBuilder);
        when(apacheBuilder.setRequestExecutor(any(HttpRequestExecutor.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setConnectionManager(any(HttpClientConnectionManager.class))).thenReturn(apacheBuilder).thenReturn(apacheBuilder);
        when(apacheBuilder.setDefaultRequestConfig(any(RequestConfig.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setDefaultSocketConfig(any(SocketConfig.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setConnectionReuseStrategy(any(ConnectionReuseStrategy.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setRetryHandler(any(HttpRequestRetryHandler.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setKeepAliveStrategy(any(ConnectionKeepAliveStrategy.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setUserAgent(any(String.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.build()).thenReturn(client);
        
        final CloseableHttpClient returnClient = builder.using(configuration).build("test"); 
        final ArgumentCaptor<RequestConfig> argument = ArgumentCaptor.forClass(RequestConfig.class);
        verify(apacheBuilder).setDefaultRequestConfig(argument.capture());

        assertThat(argument.getValue().getCookieSpec())
                .isEqualTo(CookieSpecs.IGNORE_COOKIES);
    }

    
    @Test
    public void usesBestMatchCookiePolicyIfCookiesAreEnabled() throws Exception {
        configuration.setCookiesEnabled(true);

        final org.apache.http.impl.client.HttpClientBuilder apacheBuilder = PowerMockito.mock(org.apache.http.impl.client.HttpClientBuilder.class);
        final CloseableHttpClient client = mock(CloseableHttpClient.class);
        
        PowerMockito.mockStatic(org.apache.http.impl.client.HttpClientBuilder.class);
        when(org.apache.http.impl.client.HttpClientBuilder.create()).thenReturn(apacheBuilder);
        when(apacheBuilder.setRequestExecutor(any(HttpRequestExecutor.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setConnectionManager(any(HttpClientConnectionManager.class))).thenReturn(apacheBuilder).thenReturn(apacheBuilder);
        when(apacheBuilder.setDefaultRequestConfig(any(RequestConfig.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setDefaultSocketConfig(any(SocketConfig.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setConnectionReuseStrategy(any(ConnectionReuseStrategy.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setRetryHandler(any(HttpRequestRetryHandler.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setKeepAliveStrategy(any(ConnectionKeepAliveStrategy.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setUserAgent(any(String.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.build()).thenReturn(client);
        
        final CloseableHttpClient returnClient = builder.using(configuration).build("test"); 
        final ArgumentCaptor<RequestConfig> argument = ArgumentCaptor.forClass(RequestConfig.class);
        verify(apacheBuilder).setDefaultRequestConfig(argument.capture());

        assertThat(argument.getValue().getCookieSpec())
                .isEqualTo(CookieSpecs.BEST_MATCH);
    }

    
    @Test
    public void setsTheSocketTimeout() throws Exception {
        configuration.setTimeout(Duration.milliseconds(500));

        final org.apache.http.impl.client.HttpClientBuilder apacheBuilder = PowerMockito.mock(org.apache.http.impl.client.HttpClientBuilder.class);
        final CloseableHttpClient client = mock(CloseableHttpClient.class);
        
        PowerMockito.mockStatic(org.apache.http.impl.client.HttpClientBuilder.class);
        when(org.apache.http.impl.client.HttpClientBuilder.create()).thenReturn(apacheBuilder);
        when(apacheBuilder.setRequestExecutor(any(HttpRequestExecutor.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setConnectionManager(any(HttpClientConnectionManager.class))).thenReturn(apacheBuilder).thenReturn(apacheBuilder);
        when(apacheBuilder.setDefaultRequestConfig(any(RequestConfig.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setDefaultSocketConfig(any(SocketConfig.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setConnectionReuseStrategy(any(ConnectionReuseStrategy.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setRetryHandler(any(HttpRequestRetryHandler.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setKeepAliveStrategy(any(ConnectionKeepAliveStrategy.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setUserAgent(any(String.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.build()).thenReturn(client);
        
        final CloseableHttpClient returnClient = builder.using(configuration).build("test"); 
        final ArgumentCaptor<RequestConfig> argument = ArgumentCaptor.forClass(RequestConfig.class);
        verify(apacheBuilder).setDefaultRequestConfig(argument.capture());

        assertThat(argument.getValue().getSocketTimeout())
                .isEqualTo(500);
    }

    
    @Test
    public void setsTheConnectTimeout() throws Exception {
        configuration.setConnectionTimeout(Duration.milliseconds(500));

        final org.apache.http.impl.client.HttpClientBuilder apacheBuilder = PowerMockito.mock(org.apache.http.impl.client.HttpClientBuilder.class);
        final CloseableHttpClient client = mock(CloseableHttpClient.class);
        
        PowerMockito.mockStatic(org.apache.http.impl.client.HttpClientBuilder.class);
        when(org.apache.http.impl.client.HttpClientBuilder.create()).thenReturn(apacheBuilder);
        when(apacheBuilder.setRequestExecutor(any(HttpRequestExecutor.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setConnectionManager(any(HttpClientConnectionManager.class))).thenReturn(apacheBuilder).thenReturn(apacheBuilder);
        when(apacheBuilder.setDefaultRequestConfig(any(RequestConfig.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setDefaultSocketConfig(any(SocketConfig.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setConnectionReuseStrategy(any(ConnectionReuseStrategy.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setRetryHandler(any(HttpRequestRetryHandler.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setKeepAliveStrategy(any(ConnectionKeepAliveStrategy.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setUserAgent(any(String.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.build()).thenReturn(client);
        
        final CloseableHttpClient returnClient = builder.using(configuration).build("test"); 
        final ArgumentCaptor<RequestConfig> argument = ArgumentCaptor.forClass(RequestConfig.class);
        verify(apacheBuilder).setDefaultRequestConfig(argument.capture());

        assertThat(argument.getValue().getConnectTimeout())
                .isEqualTo(500);
    }

    
    @Test
    public void disablesNaglesAlgorithm() throws Exception {
        final org.apache.http.impl.client.HttpClientBuilder apacheBuilder = PowerMockito.mock(org.apache.http.impl.client.HttpClientBuilder.class);
        final CloseableHttpClient client = mock(CloseableHttpClient.class);
        
        PowerMockito.mockStatic(org.apache.http.impl.client.HttpClientBuilder.class);
        when(org.apache.http.impl.client.HttpClientBuilder.create()).thenReturn(apacheBuilder);
        when(apacheBuilder.setRequestExecutor(any(HttpRequestExecutor.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setConnectionManager(any(HttpClientConnectionManager.class))).thenReturn(apacheBuilder).thenReturn(apacheBuilder);
        when(apacheBuilder.setDefaultRequestConfig(any(RequestConfig.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setDefaultSocketConfig(any(SocketConfig.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setConnectionReuseStrategy(any(ConnectionReuseStrategy.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setRetryHandler(any(HttpRequestRetryHandler.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setKeepAliveStrategy(any(ConnectionKeepAliveStrategy.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setUserAgent(any(String.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.build()).thenReturn(client);
        
        final CloseableHttpClient returnClient = builder.using(configuration).build("test"); 
        final ArgumentCaptor<SocketConfig> argument = ArgumentCaptor.forClass(SocketConfig.class);
        verify(apacheBuilder).setDefaultSocketConfig(argument.capture());

        assertThat(argument.getValue().isTcpNoDelay())
                .isTrue();
    }

    
    @Test
    public void disablesStaleConnectionCheck() throws Exception {
        final org.apache.http.impl.client.HttpClientBuilder apacheBuilder = PowerMockito.mock(org.apache.http.impl.client.HttpClientBuilder.class);
        final CloseableHttpClient client = mock(CloseableHttpClient.class);
        
        PowerMockito.mockStatic(org.apache.http.impl.client.HttpClientBuilder.class);
        when(org.apache.http.impl.client.HttpClientBuilder.create()).thenReturn(apacheBuilder);
        when(apacheBuilder.setRequestExecutor(any(HttpRequestExecutor.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setConnectionManager(any(HttpClientConnectionManager.class))).thenReturn(apacheBuilder).thenReturn(apacheBuilder);
        when(apacheBuilder.setDefaultRequestConfig(any(RequestConfig.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setDefaultSocketConfig(any(SocketConfig.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setConnectionReuseStrategy(any(ConnectionReuseStrategy.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setRetryHandler(any(HttpRequestRetryHandler.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setKeepAliveStrategy(any(ConnectionKeepAliveStrategy.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setUserAgent(any(String.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.build()).thenReturn(client);
        
        final CloseableHttpClient returnClient = builder.using(configuration).build("test"); 
        final ArgumentCaptor<RequestConfig> argument = ArgumentCaptor.forClass(RequestConfig.class);
        verify(apacheBuilder).setDefaultRequestConfig(argument.capture());

        assertThat(argument.getValue().isStaleConnectionCheckEnabled())
                .isFalse();
    }
    
    @Test
    public void usesACustomHttpRequestRetryHandler() throws Exception {
        HttpRequestRetryHandler customHandler = new HttpRequestRetryHandler() {
            @Override
            public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
                return false;
            }
        };

        HttpClientConfiguration config = new HttpClientConfiguration();
        config.setRetries(1);

        final org.apache.http.impl.client.HttpClientBuilder apacheBuilder = PowerMockito.mock(org.apache.http.impl.client.HttpClientBuilder.class);
        final CloseableHttpClient client = mock(CloseableHttpClient.class);
        
        PowerMockito.mockStatic(org.apache.http.impl.client.HttpClientBuilder.class);
        when(org.apache.http.impl.client.HttpClientBuilder.create()).thenReturn(apacheBuilder);
        when(apacheBuilder.setRequestExecutor(any(HttpRequestExecutor.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setConnectionManager(any(HttpClientConnectionManager.class))).thenReturn(apacheBuilder).thenReturn(apacheBuilder);
        when(apacheBuilder.setDefaultRequestConfig(any(RequestConfig.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setDefaultSocketConfig(any(SocketConfig.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setConnectionReuseStrategy(any(ConnectionReuseStrategy.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setRetryHandler(any(HttpRequestRetryHandler.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setKeepAliveStrategy(any(ConnectionKeepAliveStrategy.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setUserAgent(any(String.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.build()).thenReturn(client);
        
        final CloseableHttpClient returnClient = builder.using(config).using(customHandler).build("test"); 
        final ArgumentCaptor<HttpRequestRetryHandler> argument = ArgumentCaptor.forClass(HttpRequestRetryHandler.class);
        verify(apacheBuilder).setRetryHandler(argument.capture());        
        assertThat(argument.getValue()).isEqualTo(customHandler);
    }

    @Test
    public void usesCredentialsProvider() throws Exception {
        CredentialsProvider credentialsProvider = new CredentialsProvider() {
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

        HttpClientConfiguration config = new HttpClientConfiguration();
        config.setRetries(1);

        final org.apache.http.impl.client.HttpClientBuilder apacheBuilder = PowerMockito.mock(org.apache.http.impl.client.HttpClientBuilder.class);
        final CloseableHttpClient client = mock(CloseableHttpClient.class);
        
        PowerMockito.mockStatic(org.apache.http.impl.client.HttpClientBuilder.class);
        when(org.apache.http.impl.client.HttpClientBuilder.create()).thenReturn(apacheBuilder);
        when(apacheBuilder.setRequestExecutor(any(HttpRequestExecutor.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setConnectionManager(any(HttpClientConnectionManager.class))).thenReturn(apacheBuilder).thenReturn(apacheBuilder);
        when(apacheBuilder.setDefaultRequestConfig(any(RequestConfig.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setDefaultSocketConfig(any(SocketConfig.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setConnectionReuseStrategy(any(ConnectionReuseStrategy.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setRetryHandler(any(HttpRequestRetryHandler.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setKeepAliveStrategy(any(ConnectionKeepAliveStrategy.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.setUserAgent(any(String.class))).thenReturn(apacheBuilder);
        when(apacheBuilder.build()).thenReturn(client);
        
        final CloseableHttpClient returnClient = builder.using(config).using(credentialsProvider).build("test"); 
        final ArgumentCaptor<CredentialsProvider> argument = ArgumentCaptor.forClass(CredentialsProvider.class);
        verify(apacheBuilder).setDefaultCredentialsProvider(argument.capture());        
        assertThat(argument.getValue()).isEqualTo(credentialsProvider);
    }
}
