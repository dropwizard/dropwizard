package io.dropwizard.logging;

import ch.qos.logback.core.AsyncAppenderBase;
import ch.qos.logback.core.Context;
import ch.qos.logback.core.spi.DeferredProcessingAware;

public class DeferredProcessingAsyncAppender<E extends DeferredProcessingAware> extends AsyncAppenderBase<E> {

    public DeferredProcessingAsyncAppender(Context context) {
        setContext(context);
    }

    @Override
    protected void preprocess(E event) {
        event.prepareForDeferredProcessing();
    }
}
