package com.yammer.dropwizard.client;

import static com.google.common.base.Preconditions.checkNotNull;
import com.sun.jersey.api.client.filter.GZIPContentEncodingFilter;
import com.sun.jersey.client.apache4.ApacheHttpClient4Handler;
import com.sun.jersey.client.apache4.config.ApacheHttpClient4Config;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.jersey.JacksonMessageBodyProvider;
import com.yammer.dropwizard.json.Json;
import org.apache.http.client.HttpClient;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class JerseyClientFactory {
    private final JerseyClientConfiguration configuration;
    private final HttpClientFactory factory;
    private final ApacheHttpClient4Config config;

    public JerseyClientFactory(JerseyClientConfiguration configuration) {
        this.configuration = configuration;
        this.factory = new HttpClientFactory(configuration);
        this.config = new DefaultApacheHttpClient4Config();
    }

    /**
     * Adds the given object as a Jersey provider.
     *
     * @param provider    a Jersey provider
     */
    public JerseyClientFactory addProvider(Object provider) {
        config.getSingletons().add(checkNotNull(provider));
        return this;
    }

    /**
     * Adds the given class as a Jersey provider.
     * <p/><b>N.B.:</b> This class must either have a no-args constructor or use Jersey's built-in
     * dependency injection.
     *
     * @param klass    a Jersey provider class
     */
    public JerseyClientFactory addProvider(Class<?> klass) {
        config.getClasses().add(checkNotNull(klass));
        return this;
    }

    public JerseyClient build(Json json, ExecutorService executorService) {
        final HttpClient client = factory.build();

        final ApacheHttpClient4Handler handler = new ApacheHttpClient4Handler(client, null, true);
        config.getSingletons().add(new JacksonMessageBodyProvider(json));

        final JerseyClient jerseyClient = new JerseyClient(handler, config);
        jerseyClient.setExecutorService(executorService);

        if (configuration.isGzipEnabled()) {
            jerseyClient.addFilter(new GZIPContentEncodingFilter(configuration.isCompressRequestEntity()));
        }

        return jerseyClient;
    }

    public JerseyClient build(Environment environment) {
        Json json = environment.getService().getJson();
        ExecutorService executorService = environment.managedExecutorService("jersey-client-%d",
                                                                             configuration.getMinThreads(),
                                                                             configuration.getMaxThreads(),
                                                                             60,
                                                                             TimeUnit.SECONDS);
        return build(json, executorService);
    }
}
