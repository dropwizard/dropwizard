package io.dropwizard.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static java.util.Collections.unmodifiableList;

/**
 * @since 2.0
 *
 * @deprecated use Java 8+ JCL methods instead
 */

@Deprecated
public final class Lists {
    private Lists() {
    }

    public static <T> List<T> of(Iterable<T> elements) {
        final List<T> list = new ArrayList<>();
        for (T element : elements) {
            list.add(element);
        }
        return unmodifiableList(list);
    }

    public static <T> List<T> of(Iterator<T> it) {
        final List<T> list = new ArrayList<>();
        while (it.hasNext()) {
            list.add(it.next());
        }
        return unmodifiableList(list);
    }
}
