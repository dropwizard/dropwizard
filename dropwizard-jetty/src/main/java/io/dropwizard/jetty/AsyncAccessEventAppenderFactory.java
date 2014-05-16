package io.dropwizard.jetty;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.core.AsyncAppenderBase;
import io.dropwizard.logging.AsyncAppenderFactory;

public class AsyncAccessEventAppenderFactory implements AsyncAppenderFactory<IAccessEvent> {
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
