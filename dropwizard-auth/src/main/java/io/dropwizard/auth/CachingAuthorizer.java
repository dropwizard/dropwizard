package io.dropwizard.auth;

import com.codahale.metrics.Meter;
import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.google.common.base.Throwables;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheBuilderSpec;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.CacheStats;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Iterables;
import com.google.common.collect.Sets;
import com.google.common.util.concurrent.UncheckedExecutionException;
import org.apache.commons.lang3.tuple.ImmutablePair;

import java.security.Principal;
import java.util.function.Predicate;

import static com.codahale.metrics.MetricRegistry.name;

/**
 * An {@link Authorizer} decorator which uses a Guava {@link Cache} to
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
     * @param cacheSpec      {@link CacheBuilderSpec}
     */
    public CachingAuthorizer(
        final MetricRegistry metricRegistry,
        final Authorizer<P> authorizer,
        final CacheBuilderSpec cacheSpec
    ) {
        this(metricRegistry, authorizer, CacheBuilder.from(cacheSpec));
    }

    /**
     * Creates a new cached authorizer.
     *
     * @param metricRegistry the application's registry of metrics
     * @param authorizer     the underlying authorizer
     * @param builder        a {@link CacheBuilder}
     */
    public CachingAuthorizer(
        final MetricRegistry metricRegistry,
        final Authorizer<P> authorizer,
        final CacheBuilder<Object, Object> builder
    ) {
        this.underlying = authorizer;
        this.cacheMisses = metricRegistry.meter(name(authorizer.getClass(), "cache-misses"));
        this.getsTimer = metricRegistry.timer(name(authorizer.getClass(), "gets"));
        this.cache = builder.recordStats().build(new CacheLoader<ImmutablePair<P, String>, Boolean>() {
            @Override
            public Boolean load(ImmutablePair<P, String> key) throws Exception {
                cacheMisses.mark();
                return underlying.authorize(key.left, key.right);
            }
        });
    }

    @Override
    public boolean authorize(P principal, String role) {
        final Timer.Context context = getsTimer.time();

        try {
            final ImmutablePair<P, String> cacheKey = ImmutablePair.of(principal, role);
            return cache.getUnchecked(cacheKey);
        } catch (UncheckedExecutionException e) {
            Throwables.throwIfUnchecked(e.getCause());
            throw e;
        } finally {
            context.stop();
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
        final Predicate<ImmutablePair<P, String>> predicate =
            cacheKey -> cacheKey.getLeft().equals(principal);

        cache.invalidateAll(Sets.filter(cache.asMap().keySet(), predicate::test));
    }

    /**
     * Discards any cached role associations for the given collection
     * of principals.
     *
     * @param principals a list of principals
     */
    public void invalidateAll(Iterable<P> principals) {
        final Predicate<ImmutablePair<P, String>> predicate =
            cacheKey -> Iterables.contains(principals, cacheKey.getLeft());

        cache.invalidateAll(Sets.filter(cache.asMap().keySet(), predicate::test));
    }

    /**
     * Discards any cached role associations for principals satisfying
     * the given predicate.
     *
     * @param predicate a predicate to filter credentials
     */
    public void invalidateAll(Predicate<? super P> predicate) {
        final Predicate<ImmutablePair<P, String>> nestedPredicate =
            cacheKey -> predicate.test(cacheKey.getLeft());

        cache.invalidateAll(Sets.filter(cache.asMap().keySet(), nestedPredicate::test));
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
