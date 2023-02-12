package io.dropwizard.util;

import javax.annotation.Nullable;
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
     * Returns the innermost cause of {@code throwable}. The first throwable in a chain provides
     * context from when the error or exception was initially detected. Example usage:
     *
     * <pre>
     * assertEquals("Unable to assign a customer id", Throwables.getRootCause(e).getMessage());
     * </pre>
     *
     * @param throwable the throwable to obtain the root cause from
     * @return the root cause
     * @throws IllegalArgumentException if there is a loop in the causal chain
     * @deprecated consider using Apache commons-lang3 ExceptionUtils instead
     */
    @Deprecated
    public static Throwable getRootCause(Throwable throwable) {
        // Keep a second pointer that slowly walks the causal chain. If the fast pointer ever catches
        // the slower pointer, then there's a loop.
        Throwable slowPointer = throwable;
        boolean advanceSlowPointer = false;

        Throwable cause;
        while ((cause = throwable.getCause()) != null) {
            throwable = cause;

            if (throwable == slowPointer) {
                throw new IllegalArgumentException("Loop in causal chain detected.", throwable);
            }
            if (advanceSlowPointer) {
                slowPointer = slowPointer == null ? null : slowPointer.getCause();
            }
            advanceSlowPointer = !advanceSlowPointer; // only advance every other iteration
        }
        return throwable;
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
