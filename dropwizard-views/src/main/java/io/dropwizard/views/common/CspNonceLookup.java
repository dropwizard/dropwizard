package io.dropwizard.views.common;

import java.util.Optional;

/**
 * A utility class providing thread-local lookup for the CSP nonce
 * during the synchronous view rendering lifecycle.
 */
public final class CspNonceLookup {
    private static final ThreadLocal<String> CURRENT_NONCE = new ThreadLocal<>();

    private CspNonceLookup() {
    }

    /**
     * Returns the CSP nonce for the current rendering thread.
     *
     * @return an {@link Optional} containing the nonce, or empty if not set.
     */
    public static Optional<String> get() {
        return Optional.ofNullable(CURRENT_NONCE.get());
    }

    /**
     * Sets the CSP nonce for the current rendering thread.
     *
     * @param nonce the nonce value
     */
    public static void set(String nonce) {
        CURRENT_NONCE.set(nonce);
    }

    /**
     * Clears the CSP nonce for the current rendering thread.
     */
    public static void remove() {
        CURRENT_NONCE.remove();
    }
}
