package com.yammer.dropwizard.client;

import com.yammer.metrics.Metrics;
import com.yammer.metrics.httpclient.InstrumentedClientConnManager;
import com.yammer.metrics.httpclient.InstrumentedHttpClient;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.params.AllClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.conn.DnsResolver;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.apache.http.impl.conn.SystemDefaultDnsResolver;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.protocol.HttpContext;

import java.util.concurrent.TimeUnit;

public class HttpClientFactory {
    private final HttpClientConfiguration configuration;

    public HttpClientFactory(HttpClientConfiguration configuration) {
        this.configuration = configuration;
    }

    public HttpClient build() {
        return build(new SystemDefaultDnsResolver());
    }

    public HttpClient build(DnsResolver resolver) {
        final BasicHttpParams params = createHttpParams();
        final InstrumentedClientConnManager manager = createConnectionManager(SchemeRegistryFactory.createDefault(), resolver);
        final InstrumentedHttpClient client = new InstrumentedHttpClient(manager, params);
        setStrategiesForClient(client);

        return client;
    }

    /**
     * Add strategies to client such as ConnectionReuseStrategy and KeepAliveStrategy
     * Note that this method mutates the client object by setting the strategies
     * @param client The InstrumentedHttpClient that should be configured with strategies
     */
    private void setStrategiesForClient(InstrumentedHttpClient client) {
        final long keepAlive = configuration.getKeepAlive().toMilliseconds();

        //don't keep alive the HTTP connection and thus don't reuse the TCP socket
        if(keepAlive == 0) {
            client.setReuseStrategy(new NoConnectionReuseStrategy());
        } else {
            client.setReuseStrategy(new DefaultConnectionReuseStrategy());
            //either keep alive based on response header Keep-Alive,
            //or if the server can keep a persistent connection (-1), then override based on client's configuration
            client.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy() {
                @Override
                public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                    final long duration = super.getKeepAliveDuration(response, context);
                    return (duration == -1) ? keepAlive : duration;
                }
            });
        }
    }

    /**
     * Map the parameters in HttpClientConfiguration to a BasicHttpParams object
     * @return a BasicHttpParams object from the HttpClientConfiguration
     */
    protected BasicHttpParams createHttpParams() {
        final BasicHttpParams params = new BasicHttpParams();

        // TODO: 11/16/11 <coda> -- figure out the full set of options to support

        if (!configuration.isCookiesEnabled()) {
            params.setParameter(AllClientPNames.COOKIE_POLICY, CookiePolicy.IGNORE_COOKIES);
        } else {
            params.setParameter(AllClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH);
        }

        final Integer timeout = (int) configuration.getTimeout().toMilliseconds();
        params.setParameter(AllClientPNames.SO_TIMEOUT, timeout);

        final Integer connectionTimeout = (int) configuration.getConnectionTimeout().toMilliseconds();
        params.setParameter(AllClientPNames.CONNECTION_TIMEOUT, connectionTimeout);

        params.setParameter(AllClientPNames.TCP_NODELAY, Boolean.TRUE);
        params.setParameter(AllClientPNames.STALE_CONNECTION_CHECK, Boolean.FALSE);

        return params;
    }

    /**
     * Create a InstrumentedClientConnManager based on the HttpClientConfiguration. It sets the maximum connections per
     * route and the maximum total connections that the connection manager can create
     * @param registry the SchemeRegistry
     * @return a InstrumentedClientConnManger instance
     */
    protected InstrumentedClientConnManager createConnectionManager(SchemeRegistry registry, DnsResolver resolver) {
        final long ttl = configuration.getTimeToLive().toMilliseconds();
        final InstrumentedClientConnManager manager =
                new InstrumentedClientConnManager(Metrics.defaultRegistry(), registry, ttl, TimeUnit.MILLISECONDS, resolver);
        manager.setDefaultMaxPerRoute(configuration.getMaxConnectionsPerRoute());
        manager.setMaxTotal(configuration.getMaxConnections());
        return manager;
    }
}
