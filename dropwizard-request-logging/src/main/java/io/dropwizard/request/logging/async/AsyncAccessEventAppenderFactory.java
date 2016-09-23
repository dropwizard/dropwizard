package io.dropwizard.request.logging.async;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.AsyncAppenderBase;
import io.dropwizard.logging.async.AsyncAppenderFactory;

/**
 * An implementation of {@link AsyncAppenderFactory} for {@link IAccessEvent}.
 */
public class AsyncAccessEventAppenderFactory implements AsyncAppenderFactory<IAccessEvent> {

    /**
     * Creates an {@link AsyncAppenderFactory} of type {@link IAccessEvent} that prepares events
     * for deferred processing
     * @return the {@link AsyncAppenderFactory}
     */
    @Override
    public AsyncAppenderBase<IAccessEvent> build() {
        return new AsyncAppenderBase<IAccessEvent>() {
            @Override
            protected void preprocess(IAccessEvent event) {
                event.prepareForDeferredProcessing();
            }
        };
    }
}
