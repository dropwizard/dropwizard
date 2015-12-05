package io.dropwizard.util;

import java.util.Optional;

public abstract class Optionals {
    /**
     * Convert a Guava {@link com.google.common.base.Optional} to an {@link Optional}.
     *
     * @param guavaOptional The Guava {@link com.google.common.base.Optional}
     * @return An equivalent {@link Optional}
     */
    public static <T> Optional<T> fromGuavaOptional(final com.google.common.base.Optional<T> guavaOptional) {
        return Optional.ofNullable(guavaOptional.orNull());
    }

    /**
     * Convert an {@link Optional} to a Guava {@link com.google.common.base.Optional}.
     *
     * @param optional The {@link Optional}
     * @return An equivalent Guava {@link com.google.common.base.Optional}
     */
    public static <T> com.google.common.base.Optional<T> toGuavaOptional(final Optional<T> optional) {
        return com.google.common.base.Optional.fromNullable(optional.orElse(null));
    }
}
