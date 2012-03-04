package com.yammer.dropwizard.auth.oauth;

import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import com.yammer.dropwizard.auth.Auth;
import com.yammer.dropwizard.auth.Authenticator;

class OAuthProvider<T> implements InjectableProvider<Auth, Parameter> {
    private final Authenticator<String, T> authenticator;
    private final String realm;

    OAuthProvider(Authenticator<String, T> authenticator, String realm) {
        this.authenticator = authenticator;
        this.realm = realm;
    }

    @Override
    public ComponentScope getScope() {
        return ComponentScope.PerRequest;
    }

    @Override
    public Injectable<?> getInjectable(ComponentContext ic,
                                       Auth a,
                                       Parameter c) {
        return new OAuthInjectable<T>(authenticator, realm, a.required());
    }
}
