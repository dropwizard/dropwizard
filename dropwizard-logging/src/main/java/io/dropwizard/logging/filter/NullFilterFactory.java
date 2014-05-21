package io.dropwizard.logging.filter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;

public class NullFilterFactory<E> implements FilterFactory<E> {
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
