package io.dropwizard.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class Lists {
    private Lists() {
    }

    public static <T> List<T> of(Iterable<T> elements) {
        final List<T> list = new ArrayList<>();
        for (T element : elements) {
            list.add(element);
        }
        return list;
    }

    public static <T> List<T> of(Iterator<T> it) {
        final List<T> list = new ArrayList<>();
        while(it.hasNext()) {
            list.add(it.next());
        }
        return list;
    }
}
