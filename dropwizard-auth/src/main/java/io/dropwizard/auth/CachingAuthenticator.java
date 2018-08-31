package io.dropwizard.auth;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;

import java.security.Principal;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CompletionException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * An {@link Authenticator} decorator which uses a Guava cache to temporarily cache credentials and
 * their corresponding principals.
 *
 * @param <C> the type of credentials the authenticator can authenticate
 * @param <P> the type of principals the authenticator returns
 */
public class CachingAuthenticator<C, P extends Principal> implements Authenticator<C, P> {
    private final LoadingCache<C, Optional<P>> cache;
    private final Meter cacheMisses;
    private final Timer gets;

    /**
     * Creates a new cached authenticator.
     *
     * @param metricRegistry the application's registry of metrics
     * @param authenticator  the underlying authenticator
     * @param cacheSpec      a {@link CaffeineSpec}
     */
    public CachingAuthenticator(final MetricRegistry metricRegistry,
                                final Authenticator<C, P> authenticator,
                                final CaffeineSpec cacheSpec) {
        this(metricRegistry, authenticator, Caffeine.from(cacheSpec));
    }

    /**
     * Creates a new cached authenticator.
     *
     * @param metricRegistry the application's registry of metrics
     * @param authenticator  the underlying authenticator
     * @param builder        a {@link Caffeine}
     */
    public CachingAuthenticator(final MetricRegistry metricRegistry,
                                final Authenticator<C, P> authenticator,
                                final Caffeine<Object, Object> builder) {
        this.cacheMisses = metricRegistry.meter(name(authenticator.getClass(), "cache-misses"));
        this.gets = metricRegistry.timer(name(authenticator.getClass(), "gets"));
        this.cache = builder.recordStats().build(key -> {
            cacheMisses.mark();
            final Optional<P> optPrincipal = authenticator.authenticate(key);
            if (!optPrincipal.isPresent()) {
                // Prevent caching of unknown credentials
                throw new InvalidCredentialsException();
            }
            return optPrincipal;
        });
    }

    @Override
    public Optional<P> authenticate(C credentials) throws AuthenticationException {
        try (Timer.Context context = gets.time()) {
            return cache.get(credentials);
        } catch (CompletionException e) {
            final Throwable cause = e.getCause();
            if (cause instanceof InvalidCredentialsException) {
                return Optional.empty();
            }
            if (cause instanceof AuthenticationException) {
                throw (AuthenticationException) cause;
            }
            throw new AuthenticationException(cause);
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
        final Set<C> keys = cache.asMap().keySet().stream()
                .filter(predicate)
                .collect(Collectors.toSet());
        cache.invalidateAll(keys);
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
        return cache.estimatedSize();
    }

    /**
     * Returns a set of statistics about the cache contents and usage.
     *
     * @return a set of statistics about the cache contents and usage
     */
    public CacheStats stats() {
        return cache.stats();
    }

    /**
     * Exception thrown by {@link CacheLoader#load(Object)} when the authenticator returns {@link Optional#empty()}.
     * This is used to prevent caching of invalid credentials.
     */
    @SuppressWarnings("serial")
    private static class InvalidCredentialsException extends Exception {
    }
}
