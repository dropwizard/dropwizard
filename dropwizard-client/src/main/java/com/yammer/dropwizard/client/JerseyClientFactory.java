package com.yammer.dropwizard.client;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.sun.jersey.api.client.filter.GZIPContentEncodingFilter;
import com.sun.jersey.client.apache4.ApacheHttpClient4Handler;
import com.sun.jersey.client.apache4.config.ApacheHttpClient4Config;
import com.sun.jersey.client.apache4.config.DefaultApacheHttpClient4Config;
import com.yammer.dropwizard.config.Environment;
import com.yammer.dropwizard.jersey.JacksonMessageBodyProvider;
import org.apache.http.client.HttpClient;

import java.util.concurrent.*;

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
        config.getSingletons().add(new JacksonMessageBodyProvider());

        final JerseyClient jerseyClient = new JerseyClient(handler, config);
        jerseyClient.setExecutorService(buildThreadPool());
        environment.manage(jerseyClient);

        if (configuration.isGzipEnabled()) {
            jerseyClient.addFilter(new GZIPContentEncodingFilter());
        }

        return jerseyClient;
    }

    private ExecutorService buildThreadPool() {
        final ThreadFactory threadFactory = new ThreadFactoryBuilder().setDaemon(true)
                                                                      .setNameFormat("jersey-client-%d")
                                                                      .build();
        return new ThreadPoolExecutor(configuration.getMinThreads(),
                                                                  configuration.getMaxThreads(),
                                                                  60, TimeUnit.SECONDS,
                                                                  new SynchronousQueue<Runnable>(),
                                                                  threadFactory);
    }
}
