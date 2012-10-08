package com.yammer.dropwizard.client;

import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.client.apache4.ApacheHttpClient4;
import com.sun.jersey.client.apache4.ApacheHttpClient4Handler;

public class JerseyClient extends ApacheHttpClient4 {
    public JerseyClient(ApacheHttpClient4Handler root, ClientConfig config) {
        super(root, config);
    }
}
