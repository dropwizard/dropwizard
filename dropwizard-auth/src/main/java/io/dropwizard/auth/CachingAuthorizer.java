package io.dropwizard.auth;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.CaffeineSpec;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import io.dropwizard.util.Sets;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.security.Principal;
import java.util.Set;
import java.util.concurrent.CompletionException;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * An {@link Authorizer} decorator which uses a {@link Caffeine} cache to
 * temporarily cache principals' role associations.
 * <p>
 * Cache entries include both inclusion and exclusion of a principal
 * within a given role.
 *
 * @param <P> the type of principals on which the authorizer operates
 */
public class CachingAuthorizer<P extends Principal> implements Authorizer<P> {
    private final Authorizer<P> underlying;
    private final Meter cacheMisses;
    private final Timer getsTimer;

    // A cache which maps (principal, role) tuples to boolean
    // authorization states.
    //
    // A cached value of `true` indicates that the key's principal is
    // authorized to assume the given role. False values indicate the
    // principal is not authorized to assume the role.
    //
    // `null` cache values are interpreted as cache misses, and will
    // thus result in read through to the underlying `Authorizer`.
    private final LoadingCache<ImmutablePair<P, String>, Boolean> cache;

    /**
     * Creates a new cached authorizer.
     *
     * @param metricRegistry the application's registry of metrics
     * @param authorizer     the underlying authorizer
     * @param cacheSpec      {@link CaffeineSpec}
     */
    public CachingAuthorizer(
        final MetricRegistry metricRegistry,
        final Authorizer<P> authorizer,
        final CaffeineSpec cacheSpec) {
        this(metricRegistry, authorizer, Caffeine.from(cacheSpec));
    }

    /**
     * Creates a new cached authorizer.
     *
     * @param metricRegistry the application's registry of metrics
     * @param authorizer     the underlying authorizer
     * @param builder        a {@link CaffeineSpec}
     */
    public CachingAuthorizer(
        final MetricRegistry metricRegistry,
        final Authorizer<P> authorizer,
        final Caffeine<Object, Object> builder) {
        this.underlying = authorizer;
        this.cacheMisses = metricRegistry.meter(name(authorizer.getClass(), "cache-misses"));
        this.getsTimer = metricRegistry.timer(name(authorizer.getClass(), "gets"));
        this.cache = builder.recordStats().build(key -> {
            cacheMisses.mark();
            return underlying.authorize(key.left, key.right);
        });
    }

    @Override
    public boolean authorize(P principal, String role) {
        try (Timer.Context context = getsTimer.time()) {
            final ImmutablePair<P, String> cacheKey = ImmutablePair.of(principal, role);
            final Boolean result = cache.get(cacheKey);
            return result == null ? false : result;
        } catch (CompletionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            }
            if (cause instanceof Error) {
                throw (Error) cause;
            }
            throw e;
        }
    }

    /**
     * Discards any cached role associations for the given principal and role.
     *
     * @param principal
     * @param role
     */
    public void invalidate(P principal, String role) {
        cache.invalidate(ImmutablePair.of(principal, role));
    }

    /**
     * Discards any cached role associations for the given principal.
     *
     * @param principal
     */
    public void invalidate(P principal) {

        final Set<ImmutablePair<P, String>> keys = cache.asMap().keySet().stream()
                .filter(cacheKey -> cacheKey.getLeft().equals(principal))
                .collect(Collectors.toSet());
        cache.invalidateAll(keys);
    }

    /**
     * Discards any cached role associations for the given collection
     * of principals.
     *
     * @param principals a list of principals
     */
    public void invalidateAll(Iterable<P> principals) {
        final Set<P> principalSet = Sets.of(principals);
        final Set<ImmutablePair<P, String>> keys = cache.asMap().keySet().stream()
                .filter(cacheKey -> principalSet.contains(cacheKey.getLeft()))
                .collect(Collectors.toSet());
        cache.invalidateAll(keys);
    }

    /**
     * Discards any cached role associations for principals satisfying
     * the given predicate.
     *
     * @param predicate a predicate to filter credentials
     */
    public void invalidateAll(Predicate<? super P> predicate) {
        final Set<ImmutablePair<P, String>> keys = cache.asMap().keySet().stream()
                .filter(cacheKey -> predicate.test(cacheKey.getLeft()))
                .collect(Collectors.toSet());

        cache.invalidateAll(keys);
    }

    /**
     * Discards all cached role associations.
     */
    public void invalidateAll() {
        cache.invalidateAll();
    }

    /**
     * Returns the number of principals for which there are cached
     * role associations.
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
}
