package com.yammer.dropwizard.client;

import com.yammer.metrics.httpclient.InstrumentedClientConnManager;
import com.yammer.metrics.httpclient.InstrumentedHttpClient;
import org.apache.http.client.HttpClient;
import org.apache.http.client.params.AllClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.conn.SchemeRegistryFactory;
import org.apache.http.params.BasicHttpParams;

import java.util.concurrent.TimeUnit;

public class HttpClientFactory {
    private final HttpClientConfiguration configuration;

    public HttpClientFactory(HttpClientConfiguration configuration) {
        this.configuration = configuration;
    }

    public HttpClient build() {
        final BasicHttpParams params = new BasicHttpParams();

        // TODO: 11/16/11 <coda> -- figure out the full set of options to support

        if (!configuration.isCookiesEnabled()) {
            params.setParameter(AllClientPNames.COOKIE_POLICY, CookiePolicy.IGNORE_COOKIES);
        } else {
            params.setParameter(AllClientPNames.COOKIE_POLICY, CookiePolicy.BEST_MATCH);
        }

        final Integer timeout = (int) configuration.getTimeout().toMilliseconds();
        params.setParameter(AllClientPNames.SO_TIMEOUT, timeout);
        params.setParameter(AllClientPNames.CONNECTION_TIMEOUT, timeout);

        params.setParameter(AllClientPNames.TCP_NODELAY, Boolean.TRUE);
        params.setParameter(AllClientPNames.STALE_CONNECTION_CHECK, Boolean.FALSE);

        final InstrumentedClientConnManager manager = new InstrumentedClientConnManager(
                SchemeRegistryFactory.createDefault(),
                configuration.getTimeToLive().toMilliseconds(),
                TimeUnit.MILLISECONDS
        );
        manager.setDefaultMaxPerRoute(configuration.getMaxConnections());
        manager.setMaxTotal(configuration.getMaxConnections());

        return new InstrumentedHttpClient(manager, params);
    }
}
