package io.dropwizard.request.logging.async;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.AsyncAppenderBase;
import io.dropwizard.logging.async.AsyncAppenderFactory;

/**
 * An implementation of {@link AsyncAppenderFactory} for {@link IAccessEvent}.
 */
public class AsyncAccessEventAppenderFactory implements AsyncAppenderFactory<IAccessEvent> {

    /**
     * Creates an {@link AsyncAppenderBase} of type {@link IAccessEvent}.
     * @return the {@link AsyncAppenderBase}
     */
    @Override
    public AsyncAppenderBase<IAccessEvent> build() {
        return new AsyncAppenderBase<IAccessEvent>();
    }
}
