package io.dropwizard.auth;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.base.Optional;
import com.google.common.base.Predicate;
import com.google.common.cache.*;
import com.google.common.collect.Sets;

import java.util.concurrent.ExecutionException;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * An {@link Authenticator} decorator which uses a Guava cache to temporarily cache credentials and
 * their corresponding principals.
 *
 * @param <C> the type of credentials the authenticator can authenticate
 * @param <P> the type of principals the authenticator returns
 */
public class CachingAuthenticator<C, P> implements Authenticator<C, P> {
    private final Authenticator<C, P> underlying;
    private final LoadingCache<C, Optional<P>> cache;
    private final Meter cacheMisses;
    private final Timer gets;

    /**
     * Creates a new cached authenticator.
     *
     * @param metricRegistry the application's registry of metrics
     * @param authenticator  the underlying authenticator
     * @param cacheSpec      a {@link CacheBuilderSpec}
     */
    public CachingAuthenticator(MetricRegistry metricRegistry,
                                Authenticator<C, P> authenticator,
                                CacheBuilderSpec cacheSpec) {
        this(metricRegistry, authenticator, CacheBuilder.from(cacheSpec));
    }

    /**
     * Creates a new cached authenticator.
     *
     * @param metricRegistry the application's registry of metrics
     * @param authenticator  the underlying authenticator
     * @param builder        a {@link CacheBuilder}
     */
    public CachingAuthenticator(MetricRegistry metricRegistry,
                                Authenticator<C, P> authenticator,
                                CacheBuilder<Object, Object> builder) {
        this.underlying = authenticator;
        this.cacheMisses = metricRegistry.meter(name(authenticator.getClass(), "cache-misses"));
        this.gets = metricRegistry.timer(name(authenticator.getClass(), "gets"));
        this.cache = builder.recordStats().build(new CacheLoader<C, Optional<P>>() {
            @Override
            public Optional<P> load(C key) throws Exception {
                cacheMisses.mark();
                return underlying.authenticate(key);
            }
        });
    }

    @Override
    public Optional<P> authenticate(C credentials) throws AuthenticationException {
        final Timer.Context context = gets.time();
        try {
            return cache.get(credentials);
        } catch (ExecutionException e) {
            throw new AuthenticationException(e);
        } finally {
            context.stop();
        }
    }

    /**
     * Discards any cached principal for the given credentials.
     *
     * @param credentials a set of credentials
     */
    public void invalidate(C credentials) {
        cache.invalidate(credentials);
    }

    /**
     * Discards any cached principal for the given collection of credentials.
     *
     * @param credentials a collection of credentials
     */
    public void invalidateAll(Iterable<C> credentials) {
        cache.invalidateAll(credentials);
    }

    /**
     * Discards any cached principal for the collection of credentials satisfying the given predicate.
     *
     * @param predicate a predicate to filter credentials
     */
    public void invalidateAll(Predicate<? super C> predicate) {
    	cache.invalidateAll(Sets.filter(cache.asMap().keySet(), predicate));
    }
    
    /**
     * Discards all cached principals.
     */
    public void invalidateAll() {
        cache.invalidateAll();
    }

    /**
     * Returns the number of cached principals.
     *
     * @return the number of cached principals
     */
    public long size() {
        return cache.size();
    }

    /**
     * Returns a set of statistics about the cache contents and usage.
     *
     * @return a set of statistics about the cache contents and usage
     */
    public CacheStats stats() {
        return cache.stats();
    }
}
