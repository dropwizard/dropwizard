package io.dropwizard.auth;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.base.Predicate;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheBuilderSpec;
import com.google.common.cache.CacheStats;
import com.google.common.collect.Sets;
import java.security.Principal;
import java.util.Optional;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * An {@link Authenticator} decorator which uses a Guava cache to temporarily cache credentials and
 * their corresponding principals.
 *
 * @param <C> the type of credentials the authenticator can authenticate
 * @param <P> the type of principals the authenticator returns
 */
public class CachingAuthenticator<C, P extends Principal> implements Authenticator<C, P> {
    private final Authenticator<C, P> underlying;
    private final Cache<C, Optional<P>> cache;
    private final Meter cacheMisses;
    private final Timer gets;

    /**
     * Creates a new cached authenticator.
     *
     * @param metricRegistry the application's registry of metrics
     * @param authenticator  the underlying authenticator
     * @param cacheSpec      a {@link CacheBuilderSpec}
     */
    public CachingAuthenticator(final MetricRegistry metricRegistry,
                                final Authenticator<C, P> authenticator,
                                final CacheBuilderSpec cacheSpec) {
        this(metricRegistry, authenticator, CacheBuilder.from(cacheSpec));
    }

    /**
     * Creates a new cached authenticator.
     *
     * @param metricRegistry the application's registry of metrics
     * @param authenticator  the underlying authenticator
     * @param builder        a {@link CacheBuilder}
     */
    public CachingAuthenticator(final MetricRegistry metricRegistry,
                                final Authenticator<C, P> authenticator,
                                final CacheBuilder<Object, Object> builder) {
        this.underlying = authenticator;
        this.cacheMisses = metricRegistry.meter(name(authenticator.getClass(), "cache-misses"));
        this.gets = metricRegistry.timer(name(authenticator.getClass(), "gets"));
        this.cache = builder.recordStats().build();
    }

    @Override
    public Optional<P> authenticate(C credentials) throws AuthenticationException {
        final Timer.Context context = gets.time();
        try {
            Optional<P> optionalPrincipal = cache.getIfPresent(credentials);
            if (optionalPrincipal == null) {
                cacheMisses.mark();
                optionalPrincipal = underlying.authenticate(credentials);
                if (optionalPrincipal.isPresent()) {
                    cache.put(credentials, optionalPrincipal);
                }
            }
            return optionalPrincipal;
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
