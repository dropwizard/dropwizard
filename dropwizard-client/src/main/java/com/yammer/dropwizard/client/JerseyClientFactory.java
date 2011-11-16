package com.yammer.dropwizard.client;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import com.sun.jersey.client.apache4.ApacheHttpClient4Handler;
import com.yammer.dropwizard.config.Environment;
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

        final JerseyClient jerseyClient = new JerseyClient(handler);
        jerseyClient.setExecutorService(buildThreadPool());
        environment.manage(jerseyClient);

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
