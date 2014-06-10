package io.dropwizard.client;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableList;
import io.dropwizard.util.Duration;
import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpHeaders;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.Credentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.params.AllClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.AbstractHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.conn.PoolingClientConnectionManager;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.message.BasicHeader;
import org.apache.http.message.BasicListHeaderIterator;
import org.apache.http.protocol.HTTP;
import org.apache.http.protocol.HttpContext;
import org.junit.Test;

import java.io.IOException;
import java.lang.reflect.Field;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class HttpClientBuilderTest {
    private final HttpClientConfiguration configuration = new HttpClientConfiguration();
    private final DnsResolver resolver = mock(DnsResolver.class);
    private final HttpClientBuilder builder = new HttpClientBuilder(new MetricRegistry());
    private final SchemeRegistry registry = new SchemeRegistry();

    @Test
    public void setsTheMaximumConnectionPoolSize() throws Exception {
        configuration.setMaxConnections(412);

        final AbstractHttpClient client = (AbstractHttpClient) builder.using(configuration).build("test");
        final PoolingClientConnectionManager connectionManager = (PoolingClientConnectionManager) client.getConnectionManager();

        assertThat(connectionManager.getMaxTotal())
                .isEqualTo(412);
    }

    @Test
    public void setsTheMaximumRoutePoolSize() throws Exception {
        configuration.setMaxConnectionsPerRoute(413);

        final AbstractHttpClient client = (AbstractHttpClient) builder.using(configuration).build("test");
        final PoolingClientConnectionManager connectionManager = (PoolingClientConnectionManager) client
                .getConnectionManager();

        assertThat(connectionManager.getDefaultMaxPerRoute())
                .isEqualTo(413);
    }

    @Test
    public void setsTheUserAgent() {
        configuration.setUserAgent(Optional.of("qwerty"));

        final AbstractHttpClient client = (AbstractHttpClient) builder.using(configuration).build("test");
        assertThat(client.getParams().getParameter(AllClientPNames.USER_AGENT))
                .isEqualTo("qwerty");
    }

    @Test
    public void canUseACustomDnsResolver() throws Exception {
        // Yes, this is gross. Thanks, Apache!
        final AbstractHttpClient client = (AbstractHttpClient) builder.using(resolver).build("test");
        final Field field = PoolingClientConnectionManager.class.getDeclaredField("dnsResolver");
        field.setAccessible(true);

        assertThat(field.get(client.getConnectionManager()))
                .isEqualTo(resolver);
    }

    @Test
    public void usesASystemDnsResolverByDefault() throws Exception {
        // Yes, this is gross. Thanks, Apache!
        final AbstractHttpClient client = (AbstractHttpClient) builder.build("test");
        final Field field = PoolingClientConnectionManager.class.getDeclaredField("dnsResolver");
        field.setAccessible(true);

        assertThat(field.get(client.getConnectionManager()))
                .isInstanceOf(SystemDefaultDnsResolver.class);
    }

    @Test
    public void doesNotReuseConnectionsIfKeepAliveIsZero() throws Exception {
        configuration.setConnectionTimeout(Duration.seconds(0));

        final AbstractHttpClient client = (AbstractHttpClient) builder.using(configuration).build("test");

        assertThat(client.getConnectionReuseStrategy())
                .isInstanceOf(NoConnectionReuseStrategy.class);
    }

    @Test
    public void reusesConnectionsIfKeepAliveIsNonZero() throws Exception {
        configuration.setKeepAlive(Duration.seconds(1));

        final AbstractHttpClient client = (AbstractHttpClient) builder.using(configuration).build("test");

        assertThat(client.getConnectionReuseStrategy())
                .isInstanceOf(DefaultConnectionReuseStrategy.class);
    }

    @Test
    public void usesKeepAliveForPersistentConnections() throws Exception {
        configuration.setKeepAlive(Duration.seconds(1));

        final AbstractHttpClient client = (AbstractHttpClient) builder.using(configuration).build("test");

        final DefaultConnectionKeepAliveStrategy strategy = (DefaultConnectionKeepAliveStrategy) client.getConnectionKeepAliveStrategy();

        final HttpResponse response = mock(HttpResponse.class);
        when(response.headerIterator(HTTP.CONN_KEEP_ALIVE)).thenReturn(mock(HeaderIterator.class));

        final HttpContext context = mock(HttpContext.class);

        assertThat(strategy.getKeepAliveDuration(response, context))
                .isEqualTo(1000);
    }

    @Test
    public void usesDefaultForNonPersistentConnections() throws Exception {
        configuration.setKeepAlive(Duration.seconds(1));

        final AbstractHttpClient client = (AbstractHttpClient) builder.using(configuration).build("test");

        final DefaultConnectionKeepAliveStrategy strategy = (DefaultConnectionKeepAliveStrategy) client
                .getConnectionKeepAliveStrategy();

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
        final AbstractHttpClient client = (AbstractHttpClient) builder.using(configuration).build("test");

        assertThat(client.getParams().getParameter(AllClientPNames.COOKIE_POLICY))
                .isEqualTo(CookiePolicy.IGNORE_COOKIES);
    }

    @Test
    public void usesBestMatchCookiePolicyIfCookiesAreEnabled() throws Exception {
        configuration.setCookiesEnabled(true);

        final AbstractHttpClient client = (AbstractHttpClient) builder.using(configuration).build("test");

        assertThat(client.getParams().getParameter(AllClientPNames.COOKIE_POLICY))
                .isEqualTo(CookiePolicy.BEST_MATCH);
    }

    @Test
    public void setsTheSocketTimeout() throws Exception {
        configuration.setTimeout(Duration.milliseconds(500));

        final AbstractHttpClient client = (AbstractHttpClient) builder.using(configuration).build("test");

        assertThat(client.getParams().getIntParameter(AllClientPNames.SO_TIMEOUT, -1))
                .isEqualTo(500);
    }

    @Test
    public void setsTheConnectTimeout() throws Exception {
        configuration.setConnectionTimeout(Duration.milliseconds(500));

        final AbstractHttpClient client = (AbstractHttpClient) builder.using(configuration).build("test");

        assertThat(client.getParams().getIntParameter(AllClientPNames.CONNECTION_TIMEOUT, -1))
                .isEqualTo(500);
    }

    @Test
    public void disablesNaglesAlgorithm() throws Exception {
        final AbstractHttpClient client = (AbstractHttpClient) builder.using(configuration).build("test");

        assertThat(client.getParams().getBooleanParameter(AllClientPNames.TCP_NODELAY, false))
                .isTrue();
    }

    @Test
    public void disablesStaleConnectionCheck() throws Exception {
        final AbstractHttpClient client = (AbstractHttpClient) builder.using(configuration).build("test");

        assertThat(client.getParams().getBooleanParameter(AllClientPNames.STALE_CONNECTION_CHECK, true))
                .isFalse();
    }

    @Test
    public void usesTheDefaultSchemeRegistry() throws Exception {
        final AbstractHttpClient client = (AbstractHttpClient) builder.using(configuration).build("test");

        assertThat(client.getConnectionManager().getSchemeRegistry().getSchemeNames())
                .isEqualTo(SchemeRegistryFactory.createSystemDefault().getSchemeNames());
    }

    @Test
    public void usesACustomSchemeRegistry() throws Exception {
        final AbstractHttpClient client = (AbstractHttpClient) builder.using(registry).build("test");

        assertThat(client.getConnectionManager().getSchemeRegistry())
                .isEqualTo(registry);
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
        AbstractHttpClient client = (AbstractHttpClient) builder.using(config).using(customHandler).build("test");
        
        assertThat(client.getHttpRequestRetryHandler()).isEqualTo(customHandler);
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
        AbstractHttpClient client = (AbstractHttpClient) builder.using(config).using(credentialsProvider).build("test");

        assertThat(client.getCredentialsProvider()).isEqualTo(credentialsProvider);
    }
}
