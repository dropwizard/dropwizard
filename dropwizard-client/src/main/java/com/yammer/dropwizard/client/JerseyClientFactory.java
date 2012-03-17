package com.yammer.dropwizard.client;

import com.sun.jersey.api.client.filter.GZIPContentEncodingFilter;
import com.sun.jersey.client.apache4.ApacheHttpClient4Handler;
import com.sun.jersey.client.apache4.config.ApacheHttpClient4Config;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.jersey.JacksonMessageBodyProvider;
import org.apache.http.client.HttpClient;

import java.util.concurrent.TimeUnit;

public class JerseyClientFactory {
    private final JerseyClientConfiguration configuration;
    private final HttpClientFactory factory;

    public JerseyClientFactory(JerseyClientConfiguration configuration) {
        this.configuration = configuration;
        this.factory = new HttpClientFactory(configuration);
    }

    public JerseyClient build(Environment environment) {
        final HttpClient client = factory.build();

        final ApacheHttpClient4Handler handler = new ApacheHttpClient4Handler(client, null, true);

        final ApacheHttpClient4Config config = new DefaultApacheHttpClient4Config();
        config.getSingletons().add(new JacksonMessageBodyProvider(environment.getService().getJson()));

        final JerseyClient jerseyClient = new JerseyClient(handler, config);
        jerseyClient.setExecutorService(environment.managedExecutorService("jersey-client-%d",
                                                                           configuration.getMinThreads(),
                                                                           configuration.getMaxThreads(),
                                                                           60,
                                                                           TimeUnit.SECONDS));

        if (configuration.isGzipEnabled()) {
            jerseyClient.addFilter(new GZIPContentEncodingFilter());
        }

        return jerseyClient;
    }
}
