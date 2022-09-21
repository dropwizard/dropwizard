package io.dropwizard.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;

/**
 * Provides helper methods to create {@link Set} instances
 * @since 2.0
 *
 * @deprecated this class exists to help users transition from Guava. It will be removed in Dropwizard 3.0 in favour
 *             of Java 9+'s JCL Collection methods.
 */
@Deprecated
public final class Sets {
    private Sets() {
    }

    /**
     * Constructs an unmodifiable {@link Set} of two elements.
     *
     * @param e1 the first element
     * @param e2 the second element
     * @return the new {@link Set} instance containing the given elements
     * @param <T> the type of the set elements
     */
    public static <T> Set<T> of(T e1, T e2) {
        final Set<T> set = new HashSet<>(2);
        set.add(e1);
        set.add(e2);
        return unmodifiableSet(set);
    }

    /**
     * Constructs an unmodifiable {@link Set} of three elements.
     *
     * @param e1 the first element
     * @param e2 the second element
     * @param e3 the third element
     * @return the new {@link Set} instance containing the given elements
     * @param <T> the type of the set elements
     */
    public static <T> Set<T> of(T e1, T e2, T e3) {
        final Set<T> set = new HashSet<>(3);
        set.add(e1);
        set.add(e2);
        set.add(e3);
        return unmodifiableSet(set);
    }

    /**
     * Constructs an unmodifiable {@link Set} of four elements.
     *
     * @param e1 the first element
     * @param e2 the second element
     * @param e3 the third element
     * @param e4 the fourth element
     * @return the new {@link Set} instance containing the given elements
     * @param <T> the type of the set elements
     */
    public static <T> Set<T> of(T e1, T e2, T e3, T e4) {
        final Set<T> set = new HashSet<>(4);
        set.add(e1);
        set.add(e2);
        set.add(e3);
        set.add(e4);
        return unmodifiableSet(set);
    }

    /**
     * Constructs an unmodifiable {@link Set} of five elements.
     *
     * @param e1 the first element
     * @param e2 the second element
     * @param e3 the third element
     * @param e4 the fourth element
     * @param e5 the fifth element
     * @return the new {@link Set} instance containing the given elements
     * @param <T> the type of the set elements
     */
    public static <T> Set<T> of(T e1, T e2, T e3, T e4, T e5) {
        final Set<T> set = new HashSet<>(5);
        set.add(e1);
        set.add(e2);
        set.add(e3);
        set.add(e4);
        set.add(e5);
        return unmodifiableSet(set);
    }

    /**
     * Constructs an unmodifiable {@link Set} of a variable number of elements.
     *
     * @param elements the elements to create the set of
     * @return the new {@link Set} instance containing the given elements
     * @param <T> the type of the set elements
     */
    @SafeVarargs
    public static <T> Set<T> of(T... elements) {
        final Set<T> set = new HashSet<>(elements.length);
        set.addAll(Arrays.asList(elements));
        return unmodifiableSet(set);
    }

    /**
     * Constructs an unmodifiable {@link Set} of an {@link Iterable} of elements.
     *
     * @param elements the elements to create the set of
     * @return the new {@link Set} instance containing the given elements
     * @param <T> the type of the set elements
     */
    public static <T> Set<T> of(Iterable<T> elements) {
        final Set<T> set = new HashSet<>();
        for (T element : elements) {
            set.add(element);
        }
        return unmodifiableSet(set);
    }
}
