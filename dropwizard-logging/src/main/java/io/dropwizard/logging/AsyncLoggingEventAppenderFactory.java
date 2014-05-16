package io.dropwizard.logging;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AsyncAppenderBase;

public class AsyncLoggingEventAppenderFactory implements AsyncAppenderFactory<ILoggingEvent> {
    @Override
    public AsyncAppenderBase<ILoggingEvent> build() {
        return new AsyncAppender();
    }
}
