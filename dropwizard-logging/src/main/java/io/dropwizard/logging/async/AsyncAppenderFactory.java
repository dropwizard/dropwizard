package io.dropwizard.logging.async;

import ch.qos.logback.core.AsyncAppenderBase;
import ch.qos.logback.core.spi.DeferredProcessingAware;

/**
 * Factory used to create an {@link AsyncAppenderBase} of type E
 * @param <E> The type of log event
 */
public interface AsyncAppenderFactory<E extends DeferredProcessingAware> {

    /**
     * Creates an {@link AsyncAppenderBase} of type E
     * @return a new {@link AsyncAppenderBase}
     */
    AsyncAppenderBase<E> build();
}
