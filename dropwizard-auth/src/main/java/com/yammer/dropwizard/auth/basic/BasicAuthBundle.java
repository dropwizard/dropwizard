package com.yammer.dropwizard.auth.basic;

import com.yammer.dropwizard.Bundle;
import com.yammer.dropwizard.auth.Authenticator;
import com.yammer.dropwizard.config.Environment;

public class BasicAuthBundle<T> implements Bundle {
    private final Authenticator<BasicCredentials, T> authenticator;
    private final String realm;

    public BasicAuthBundle(Authenticator<BasicCredentials, T> authenticator, String realm) {
        this.authenticator = authenticator;
        this.realm = realm;
    }

    @Override
    public void initialize(Environment environment) {
        environment.addProvider(new BasicAuthProvider<T>(authenticator, realm));
    }
}
