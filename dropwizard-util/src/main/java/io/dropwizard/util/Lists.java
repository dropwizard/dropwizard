package io.dropwizard.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.util.Collections.unmodifiableList;

/**
 * Helper methods to construct {@link List} instances.
 * @since 2.0
 *
 * @deprecated use Java 8+ JCL methods instead
 */

@Deprecated
public final class Lists {
    private Lists() {
    }

    /**
     * Constructs an unmodifiable {@link List} of an {@link Iterable} of elements.
     *
     * @param elements the elements to construct the list of
     * @return the list containing the given elements
     * @param <T> the type of the list elements
     */
    public static <T> List<T> of(Iterable<T> elements) {
        final List<T> list = new ArrayList<>();
        for (T element : elements) {
            list.add(element);
        }
        return unmodifiableList(list);
    }

    /**
     * Constructs an unmodifiable {@link List} of an {@link Iterator} of elements.
     *
     * @param it the elements to construct the list of
     * @return the list containing the given elements
     * @param <T> the type of the list elements
     */
    public static <T> List<T> of(Iterator<T> it) {
        final List<T> list = new ArrayList<>();
        while (it.hasNext()) {
            list.add(it.next());
        }
        return unmodifiableList(list);
    }
}
