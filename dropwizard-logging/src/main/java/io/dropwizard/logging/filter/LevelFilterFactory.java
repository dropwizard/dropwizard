package io.dropwizard.logging.filter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.DeferredProcessingAware;

/**
 * An interface for building Logback {@link Filter Filters} with a specified {@link Level}.
 * @param <E> The type of log event
 */
public interface LevelFilterFactory<E extends DeferredProcessingAware> {

    /**
     * Creates a {@link Filter} of type E
     * @return a new {@link Filter}
     */
    Filter<E> build(Level threshold);
}
