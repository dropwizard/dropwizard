package io.dropwizard.util;

import org.jspecify.annotations.Nullable;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Provides helper methods to work with {@link Throwable} objects.
 *
 * @since 2.0
 */
public final class Throwables {
    private Throwables() {
    }

    /**
     * Search an exception chain for an exception matching a given condition.
     *
     * @param condition The condition to match on
     * @param t The head of the exception chain
     * @return An {@link Optional} containing the first match in the chain, starting from the head, or empty if no
     *         matching exception was found
     * @since 2.1.0
     */
    public static Optional<Throwable> findThrowableInChain(Predicate<Throwable> condition, @Nullable Throwable t) {
        final Set<Throwable> seen = new HashSet<>();
        while (t != null && !seen.contains(t)) {
            if (condition.test(t)) {
                return Optional.of(t);
            }
            seen.add(t);
            t = t.getCause();
        }
        return Optional.empty();
    }
}
