package io.dropwizard.client;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.httpclient.InstrumentedHttpClientConnectionManager;
import com.codahale.metrics.httpclient.InstrumentedHttpRequestExecutor;
import com.google.common.annotations.VisibleForTesting;
import io.dropwizard.setup.Environment;
import org.apache.http.ConnectionReuseStrategy;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.HttpRequestRetryHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.SocketConfig;
import org.apache.http.impl.DefaultConnectionReuseStrategy;
import org.apache.http.impl.NoConnectionReuseStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultConnectionKeepAliveStrategy;
import org.apache.http.impl.client.DefaultHttpRequestRetryHandler;
import org.apache.http.protocol.HttpContext;

import java.io.IOException;

/**
 * A convenience class for building {@link HttpClient} instances.
 * <p>
 * Among other things,
 * <ul>
 * <li>Disables stale connection checks</li>
 * <li>Disables Nagle's algorithm</li>
 * <li>Disables cookie management by default</li>
 * </ul>
 * </p>
 */
public class HttpClientBuilder extends ApacheClientBuilderBase<HttpClientBuilder, HttpClientConfiguration> {
    private static final HttpRequestRetryHandler NO_RETRIES = new HttpRequestRetryHandler() {
        @Override
        public boolean retryRequest(IOException exception, int executionCount, HttpContext context) {
            return false;
        }
    };

    public HttpClientBuilder(MetricRegistry metricRegistry) {
        super(metricRegistry, new HttpClientConfiguration());
    }

    public HttpClientBuilder(Environment environment) {
        super(environment, new HttpClientConfiguration());
    }

    /**
     * Builds the {@link HttpClient}.
     *
     * @param name
     * @return an {@link CloseableHttpClient}
     */
    public CloseableHttpClient build(String name) {
        final InstrumentedHttpClientConnectionManager manager = createConnectionManager(registry, name);
        return createClient(org.apache.http.impl.client.HttpClientBuilder.create(), manager, name);
    }

    /**
     * Map the parameters in {@link HttpClientConfiguration} to configuration on a
     * {@link org.apache.http.impl.client.HttpClientBuilder} instance
     *
     * @param builder
     * @param manager
     * @param name
     * @return the configured {@link CloseableHttpClient}
     */
    @VisibleForTesting
    protected CloseableHttpClient createClient(
            final org.apache.http.impl.client.HttpClientBuilder builder,
            final InstrumentedHttpClientConnectionManager manager,
            final String name) {
        final Integer timeout = (int) configuration.getTimeout().toMilliseconds();
        final long keepAlive = configuration.getKeepAlive().toMilliseconds();
        final ConnectionReuseStrategy reuseStrategy = keepAlive == 0
                ? new NoConnectionReuseStrategy()
                : new DefaultConnectionReuseStrategy();
        final HttpRequestRetryHandler retryHandler = configuration.getRetries() == 0
                ? NO_RETRIES
                : (httpRequestRetryHandler == null ? new DefaultHttpRequestRetryHandler(configuration.getRetries(),
                false) : httpRequestRetryHandler);

        final RequestConfig requestConfig = createRequestConfig();
        final SocketConfig socketConfig = SocketConfig.custom()
                .setTcpNoDelay(true)
                .setSoTimeout(timeout)
                .build();

        builder.setRequestExecutor(new InstrumentedHttpRequestExecutor(metricRegistry, metricNameStrategy, name))
                .setConnectionManager(manager)
                .setDefaultRequestConfig(requestConfig)
                .setDefaultSocketConfig(socketConfig)
                .setConnectionReuseStrategy(reuseStrategy)
                .setRetryHandler(retryHandler)
                .setUserAgent(createUserAgent(name));

        if (keepAlive != 0) {
            // either keep alive based on response header Keep-Alive,
            // or if the server can keep a persistent connection (-1), then override based on client's configuration
            builder.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy() {
                @Override
                public long getKeepAliveDuration(HttpResponse response, HttpContext context) {
                    final long duration = super.getKeepAliveDuration(response, context);
                    return (duration == -1) ? keepAlive : duration;
                }
            });
        }

        if (credentialsProvider != null) {
            builder.setDefaultCredentialsProvider(credentialsProvider);
        }

        if (routePlanner != null) {
            builder.setRoutePlanner(routePlanner);
        }

        return builder.build();
    }

    /**
     * Create a user agent string using the configured user agent if defined, otherwise
     * using a combination of the environment name and this client name
     *
     * @param name the name of this client
     * @return the user agent string to be used by this client
     */
    protected String createUserAgent(String name) {
        final String defaultUserAgent = this.name == null ? name : String.format("%s (%s)", this.name, name);
        return configuration.getUserAgent().or(defaultUserAgent);
    }


}
