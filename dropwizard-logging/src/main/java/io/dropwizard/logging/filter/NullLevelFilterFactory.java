package io.dropwizard.logging.filter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.DeferredProcessingAware;
import ch.qos.logback.core.spi.FilterReply;

/**
 * Factory for building a logback {@link Filter} that will always defer to the next Filter.
 * @param <E> The type of log event
 */
public class NullLevelFilterFactory<E extends DeferredProcessingAware> implements LevelFilterFactory<E> {

    /**
     * Creates a {@link Filter} that will always defer to the next Filter in the chain, if any.
     * @param threshold the parameter is ignored
     * @return a {@link Filter} with a {@link Filter#decide(Object)} method that will always return {@link FilterReply#NEUTRAL}.
     */
    @Override
    public Filter<E> build(Level threshold) {
        return new Filter<E>() {
            @Override
            public FilterReply decide(E event) {
                return FilterReply.NEUTRAL;
            }
        };
    }
}
