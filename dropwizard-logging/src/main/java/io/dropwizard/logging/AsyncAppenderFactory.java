package io.dropwizard.logging;

import ch.qos.logback.core.AsyncAppenderBase;

public interface AsyncAppenderFactory<E> {
    AsyncAppenderBase<E> build();
}
