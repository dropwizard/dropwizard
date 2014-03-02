package io.dropwizard.auth;

import org.glassfish.jersey.server.internal.inject.AbstractContainerRequestValueFactory;
import org.glassfish.hk2.utilities.Binder;

public abstract class AuthFactory<C,P> extends AbstractContainerRequestValueFactory<P> {

    private Authenticator<C, P> authenticator;

    public AuthFactory(Authenticator<C, P> authenticator) {
        this.authenticator = authenticator;
    }

    public abstract AuthFactory<C,P> clone (boolean required);

    public abstract Class<P> getGeneratedClass();

    public Authenticator<C, P> authenticator() {
        return authenticator;
    }

    public static <T,U> Binder binder (AuthFactory<T,U> factory)
    {
        return new AuthFactoryProvider.Binder<>(factory);
    }
}
