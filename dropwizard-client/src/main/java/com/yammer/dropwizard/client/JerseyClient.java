package com.yammer.dropwizard.client;

import com.sun.jersey.client.apache4.ApacheHttpClient4;
import com.sun.jersey.client.apache4.ApacheHttpClient4Handler;
import com.yammer.dropwizard.lifecycle.Managed;

import java.util.concurrent.TimeUnit;

public class JerseyClient extends ApacheHttpClient4 implements Managed {
    public JerseyClient(ApacheHttpClient4Handler handler) {
        super(handler);
    }

    @Override
    public void start() throws Exception {
        // already started man
    }

    @Override
    public void stop() throws Exception {
        getExecutorService().shutdown();
        getExecutorService().awaitTermination(1, TimeUnit.MINUTES);
        destroy();
    }
}
