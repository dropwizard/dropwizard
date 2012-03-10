package com.yammer.dropwizard.auth.basic;

import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import com.yammer.dropwizard.auth.Auth;
import com.yammer.dropwizard.auth.Authenticator;

class BasicAuthProvider<T> implements InjectableProvider<Auth, Parameter> {
    private final Authenticator<BasicCredentials, T> authenticator;
    private final String realm;

    BasicAuthProvider(Authenticator<BasicCredentials, T> authenticator, String realm) {
        this.authenticator = authenticator;
        this.realm = realm;
    }

    public Authenticator<BasicCredentials, T> getAuthenticator() {
        return authenticator;
    }

    public String getRealm() {
        return realm;
    }

    @Override
    public ComponentScope getScope() {
        return ComponentScope.PerRequest;
    }

    @Override
    public Injectable<?> getInjectable(ComponentContext ic,
                                       Auth a,
                                       Parameter c) {
        return new BasicAuthInjectable<T>(authenticator, realm, a.required());
    }
}
