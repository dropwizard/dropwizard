package com.codahale.dropwizard.client;

import com.codahale.dropwizard.setup.Environment;
import com.codahale.dropwizard.util.Duration;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.httpclient.InstrumentedClientConnManager;
import com.codahale.metrics.httpclient.InstrumentedHttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.params.AllClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * A convenience class for building {@link HttpClient} instances.
 * <p>
 * Among other things,
 * <ul>
 *     <li>Disables stale connection checks</li>
 *     <li>Disables Nagle's algorithm</li>
 *     <li>Disables cookie management by default</li>
 * </ul>
 */
public class HttpClientBuilder {
    private static final HttpRequestRetryHandler NO_RETRIES = new HttpRequestRetryHandler() {
        @Override
        public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
            return false;
        }
    };

    private final MetricRegistry metricRegistry;
    private HttpClientConfiguration configuration = new HttpClientConfiguration();
    private DnsResolver resolver = new SystemDefaultDnsResolver();
    private SchemeRegistry registry = SchemeRegistryFactory.createSystemDefault();

    public HttpClientBuilder(MetricRegistry metricRegistry) {
        this.metricRegistry = metricRegistry;
    }

    public HttpClientBuilder(Environment environment) {
        this.metricRegistry = environment.metrics();
    }

    /**
     * Use the given {@link HttpClientConfiguration} instance.
     *
     * @param configuration    a {@link HttpClientConfiguration} instance
     * @return {@code this}
     */
    public HttpClientBuilder using(HttpClientConfiguration configuration) {
        this.configuration = configuration;
        return this;
    }

    /**
     * Use the given {@link DnsResolver} instance.
     *
     * @param resolver    a {@link DnsResolver} instance
     * @return {@code this}
     */
    public HttpClientBuilder using(DnsResolver resolver) {
        this.resolver = resolver;
        return this;
    }

    /**
     * Use the given {@link SchemeRegistry} instance.
     *
     * @param registry    a {@link SchemeRegistry} instance
     * @return {@code this}
     */
    public HttpClientBuilder using(SchemeRegistry registry) {
        this.registry = registry;
        return this;
    }

    /**
     * Builds the {@link HttpClient}.
     *
     * @return an {@link HttpClient}
     */
    public HttpClient build(String name) {
        final BasicHttpParams params = createHttpParams();
        final InstrumentedClientConnManager manager = createConnectionManager(registry, name);
        final InstrumentedHttpClient client = new InstrumentedHttpClient(metricRegistry, manager, params, name);
        setStrategiesForClient(client);

        return client;
    }

    /**
     * Add strategies to client such as ConnectionReuseStrategy and KeepAliveStrategy Note that this
     * method mutates the client object by setting the strategies
     *
     * @param client The InstrumentedHttpClient that should be configured with strategies
     */
    protected void setStrategiesForClient(InstrumentedHttpClient client) {
        final long keepAlive = configuration.getKeepAlive().toMilliseconds();

        // don't keep alive the HTTP connection and thus don't reuse the TCP socket
        if (keepAlive == 0) {
            client.setReuseStrategy(new NoConnectionReuseStrategy());
        } else {
            client.setReuseStrategy(new DefaultConnectionReuseStrategy());
            // either keep alive based on response header Keep-Alive,
            // or if the server can keep a persistent connection (-1), then override based on client's configuration
            client.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy() {
                @Override
                public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                    final long duration = super.getKeepAliveDuration(response, context);
                    return (duration == -1) ? keepAlive : duration;
                }
            });
        }

        if (configuration.getRetries() == 0) {
            client.setHttpRequestRetryHandler(NO_RETRIES);
        } else {
            client.setHttpRequestRetryHandler(new DefaultHttpRequestRetryHandler(configuration.getRetries(),
                                                                                 false));
        }
    }

    /**
     * Map the parameters in HttpClientConfiguration to a BasicHttpParams object
     *
     * @return a BasicHttpParams object from the HttpClientConfiguration
     */
    protected BasicHttpParams createHttpParams() {
        final BasicHttpParams params = new BasicHttpParams();

        if (configuration.isCookiesEnabled()) {
            params.setParameter(AllClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH);
        } else {
            params.setParameter(AllClientPNames.COOKIE_POLICY, CookiePolicy.IGNORE_COOKIES);
        }

        final Integer timeout = (int) configuration.getTimeout().toMilliseconds();
        params.setParameter(AllClientPNames.SO_TIMEOUT, timeout);

        final Integer connectionTimeout = (int) configuration.getConnectionTimeout()
                                                             .toMilliseconds();
        params.setParameter(AllClientPNames.CONNECTION_TIMEOUT, connectionTimeout);

        params.setParameter(AllClientPNames.TCP_NODELAY, Boolean.TRUE);
        params.setParameter(AllClientPNames.STALE_CONNECTION_CHECK, Boolean.FALSE);

        return params;
    }

    /**
     * Create a InstrumentedClientConnManager based on the HttpClientConfiguration. It sets the
     * maximum connections per route and the maximum total connections that the connection manager
     * can create
     *
     * @param registry the SchemeRegistry
     * @return a InstrumentedClientConnManger instance
     */
    protected InstrumentedClientConnManager createConnectionManager(SchemeRegistry registry, String name) {
        final Duration ttl = configuration.getTimeToLive();
        final InstrumentedClientConnManager manager =
                new InstrumentedClientConnManager(metricRegistry,
                                                  registry,
                                                  ttl.getQuantity(),
                                                  ttl.getUnit(),
                                                  resolver,
                                                  name);
        manager.setDefaultMaxPerRoute(configuration.getMaxConnectionsPerRoute());
        manager.setMaxTotal(configuration.getMaxConnections());
        return manager;
    }
}
