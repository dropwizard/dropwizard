package io.dropwizard.logging.filter;

import ch.qos.logback.classic.Level;
import ch.qos.logback.core.filter.Filter;

public interface FilterFactory<E> {
    Filter<E> build(Level threshold);
}
