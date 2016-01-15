package io.dropwizard.logging;

import ch.qos.logback.core.AsyncAppenderBase;

/**
 * Factory used to create an {@link AsyncAppenderBase} of type E
 * @param <E> The type of log event
 */
public interface AsyncAppenderFactory<E> {

    /**
     * Creates an {@link AsyncAppenderBase} of type E
     * @return a new {@link AsyncAppenderBase}
     */
    AsyncAppenderBase<E> build();
}
