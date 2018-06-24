package io.dropwizard.util;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static java.util.Collections.unmodifiableSet;

public final class Sets {
    private Sets() {
    }

    public static <T> Set<T> of(T e1, T e2) {
        final Set<T> set = new HashSet<>(2);
        set.add(e1);
        set.add(e2);
        return unmodifiableSet(set);
    }

    public static <T> Set<T> of(T e1, T e2, T e3) {
        final Set<T> set = new HashSet<>(3);
        set.add(e1);
        set.add(e2);
        set.add(e3);
        return unmodifiableSet(set);
    }

    public static <T> Set<T> of(T e1, T e2, T e3, T e4) {
        final Set<T> set = new HashSet<>(4);
        set.add(e1);
        set.add(e2);
        set.add(e3);
        set.add(e4);
        return unmodifiableSet(set);
    }

    public static <T> Set<T> of(T e1, T e2, T e3, T e4, T e5) {
        final Set<T> set = new HashSet<>(5);
        set.add(e1);
        set.add(e2);
        set.add(e3);
        set.add(e4);
        set.add(e5);
        return unmodifiableSet(set);
    }

    @SafeVarargs
    public static <T> Set<T> of(T... elements) {
        final Set<T> set = new HashSet<>(elements.length);
        set.addAll(Arrays.asList(elements));
        return unmodifiableSet(set);
    }

    public static <T> Set<T> of(Iterable<T> elements) {
        final Set<T> set = new HashSet<>();
        for (T element : elements) {
            set.add(element);
        }
        return unmodifiableSet(set);
    }
}
