package io.dropwizard.auth;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * A Jersey provider that chains various authentication providers like {@link io.dropwizard.auth.oauth.OAuthFactory}
 * and {@link io.dropwizard.auth.basic.BasicAuthFactory}.
 *
 * @param <T> the principal type
 */
public class ChainedAuthFactory<T> extends AuthFactory<Object, T> {
    private List<AuthFactory<? extends Object, T>> factories = null;

    public ChainedAuthFactory() {
        this(new ArrayList<AuthFactory<? extends Object, T>>());
    }

    public ChainedAuthFactory(AuthFactory<? extends Object, T>... providers) {
        this(Arrays.asList(providers));
    }

    public ChainedAuthFactory(List<AuthFactory<? extends Object, T>> factories) {
        super(null);
        this.factories = factories;
    }

    @Override
    public Authenticator<Object, T> authenticator() {
        return super.authenticator();
    }

    /**
     * Add an auth provider into the chain.
     *
     * @param provider
     * @return true if the provider was added.
     */
    public boolean addChainedProvider(AuthFactory<? extends Object, T> provider) {
        return this.factories.add(provider);
    }

    /**
     * Removes an auth provider from the chain.
     *
     * @param provider
     * @return true if the provider was removed.
     */
    public boolean removeChainedProvider(AuthFactory<? extends Object, T> provider) {
        return this.factories.remove(provider);
    }

    @Override
    public AuthFactory<Object, T> clone(boolean required) {
        ChainedAuthFactory<T> clone = new ChainedAuthFactory<>();
        for (AuthFactory<? extends Object, T> factory : factories) {
            clone.addChainedProvider(factory.clone(true));
        }
        return clone;
    }

    @Override
    public Class<T> getGeneratedClass() {
        Class<T> generatedClass = null;
        for (AuthFactory<? extends Object, T> factory : factories) {
            if (generatedClass == null || generatedClass == factory.getGeneratedClass()) {
                generatedClass = factory.getGeneratedClass();
            } else {
                throw new WebApplicationException("Chained auth factories must " +
                        "have the same generated class.");
            }
        }
        return generatedClass;
    }

    @Context
    @Override
    public void setRequest(HttpServletRequest request) {
        for (AuthFactory<? extends Object, T> factory : factories) {
            factory.setRequest(request);
        }
    }

    @Override
    public T provide() {
        WebApplicationException firstException = null;
        for (AuthFactory<? extends Object, T> factory : factories) {
            try {
                T value = factory.provide();
                if (value != null) {
                    return value;
                }
            } catch (WebApplicationException e) {
                if (firstException == null) {
                    firstException = e;
                }
            }
        }
        if (firstException == null) {
            return null;
        }
        throw firstException;
    }

}
