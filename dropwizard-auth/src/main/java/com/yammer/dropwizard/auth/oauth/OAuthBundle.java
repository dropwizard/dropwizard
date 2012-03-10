package com.yammer.dropwizard.auth.oauth;

import com.yammer.dropwizard.Bundle;
import com.yammer.dropwizard.auth.Authenticator;
import com.yammer.dropwizard.config.Environment;

/**
 * A bundle which provides OAuth2 authentication support for bearer tokens.
 *
 * @param <T>    the principal type
 */
public class OAuthBundle<T> implements Bundle {
    private final Authenticator<String, T> authenticator;
    private final String realm;

    /**
     * Creates a new bundle with the given authentication and realm.
     *
     * @param authenticator    the authenticator for converting tokens to principals
     * @param realm            the name of the realm
     */
    public OAuthBundle(Authenticator<String, T> authenticator, String realm) {
        this.authenticator = authenticator;
        this.realm = realm;
    }

    @Override
    public void initialize(Environment environment) {
        environment.addProvider(new OAuthProvider<T>(authenticator, realm));
    }
}
