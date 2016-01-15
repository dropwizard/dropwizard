package io.dropwizard.logging.filter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.core.filter.Filter;

/**
 * An interface for building Logback {@link Filter Filters}
 * @param <E> The type of log event
 */
public interface FilterFactory<E> {

    /**
     * Creates a {@link Filter} of type E
     * @return a new {@link Filter}
     */
    Filter<E> build(Level threshold);
}
