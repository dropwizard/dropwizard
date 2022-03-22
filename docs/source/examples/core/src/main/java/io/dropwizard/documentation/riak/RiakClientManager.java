package io.dropwizard.documentation.riak;

import io.dropwizard.lifecycle.Managed;

public class RiakClientManager implements Managed {
    private final RiakClient client;

    public RiakClientManager(RiakClient client) {
        this.client = client;
    }

    @Override
    public void start() throws Exception {
        client.start();
    }

    @Override
    public void stop() throws Exception {
        client.stop();
    }
}