package com.yammer.dropwizard.auth.basic;

import com.yammer.dropwizard.Bundle;
import com.yammer.dropwizard.auth.Authenticator;
import com.yammer.dropwizard.config.Environment;

/**
 * A bundle which provides Basic HTTP Authentication support.
 *
 * @param <T>    the principal type
 */
public class BasicAuthBundle<T> implements Bundle {
    private final Authenticator<BasicCredentials, T> authenticator;
    private final String realm;

    /**
     * Creates a new bundle with the given authenticator and realm.
     *
     * @param authenticator    the authenticator for converting usernames and passwords into
     *                         principals
     * @param realm            the name of the realm
     */
    public BasicAuthBundle(Authenticator<BasicCredentials, T> authenticator, String realm) {
        this.authenticator = authenticator;
        this.realm = realm;
    }

    @Override
    public void initialize(Environment environment) {
        environment.addProvider(new BasicAuthProvider<T>(authenticator, realm));
    }
}
