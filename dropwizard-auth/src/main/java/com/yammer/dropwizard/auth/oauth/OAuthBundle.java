package com.yammer.dropwizard.auth.oauth;

import com.yammer.dropwizard.Bundle;
import com.yammer.dropwizard.auth.Authenticator;
import com.yammer.dropwizard.config.Environment;

public class OAuthBundle<T> implements Bundle {
    private final Authenticator<String, T> authenticator;
    private final String realm;

    public OAuthBundle(Authenticator<String, T> authenticator, String realm) {
        this.authenticator = authenticator;
        this.realm = realm;
    }

    @Override
    public void initialize(Environment environment) {
        environment.addProvider(new OAuthProvider<T>(authenticator, realm));
    }
}
