package com.yammer.dropwizard.auth.basic;

import com.sun.jersey.api.model.Parameter;
import com.sun.jersey.core.spi.component.ComponentContext;
import com.sun.jersey.core.spi.component.ComponentScope;
import com.sun.jersey.spi.inject.Injectable;
import com.sun.jersey.spi.inject.InjectableProvider;
import com.yammer.dropwizard.auth.Auth;
import com.yammer.dropwizard.auth.Authenticator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Jersey provider for Basic HTTP authentication.
 *
 * @param <T>    the principal type.
 */
public class BasicAuthProvider<T> implements InjectableProvider<Auth, Parameter> {
    static final Logger LOGGER = LoggerFactory.getLogger(BasicAuthProvider.class);

    private final Authenticator<BasicCredentials, T> authenticator;
    private final String realm;

    /**
     * Creates a new {@link BasicAuthProvider} with the given {@link Authenticator} and realm.
     *
     * @param authenticator    the authenticator which will take the {@link BasicCredentials} and
     *                         convert them into instances of {@code T}
     * @param realm            the name of the authentication realm
     */
    public BasicAuthProvider(Authenticator<BasicCredentials, T> authenticator, String realm) {
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
        return new BasicAuthInjectable<T>(authenticator, realm, a.required());
    }
}
