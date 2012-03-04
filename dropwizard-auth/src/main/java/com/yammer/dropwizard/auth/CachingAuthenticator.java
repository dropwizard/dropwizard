package com.yammer.dropwizard.auth;

import com.google.common.base.Optional;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import java.util.concurrent.ExecutionException;

public class CachingAuthenticator<C, P> implements Authenticator<C, P> {
    private final Authenticator<C, P> underlying;
    private final LoadingCache<C, Optional<P>> cache;

    public CachingAuthenticator(Authenticator<C, P> authenticator,
                                CacheBuilder<Object, Object> builder) {
        this.underlying = authenticator;
        this.cache = builder.build(new CacheLoader<C, Optional<P>> () {
            @Override
            public Optional<P> load(C key) throws Exception {
                return underlying.authenticate(key);
            }
        });
    }

    @Override
    public Optional<P> authenticate(C credentials) throws AuthenticationException {
        try {
            return cache.get(credentials);
        } catch (ExecutionException e) {
            throw new AuthenticationException(e);
        }
    }
}
