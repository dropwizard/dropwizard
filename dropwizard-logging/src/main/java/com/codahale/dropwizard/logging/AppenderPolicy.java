package com.codahale.dropwizard.logging;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * Defines the form that the logging channel takes, in some cases for high performance services
 * it can be useful to have a logger that is out of band with the main application and logs
 * from a queue, some applications require logging immediately, some applications require
 * special confirmation etc.
 */
@JsonFormat(shape = JsonFormat.Shape.OBJECT)
public enum AppenderPolicy {

    /**
     * The appender as configured will log synchronously to disk inline with the application
     */
    SYNC(false, false, false),

    /**
     * The appender will make best efforts to log asynchronously, but if the logging queue overfills
     * the application will fallback to synchronous logging,
     */
    ASYNC(true, false, false),

    /**
     * The appender will make best efforts to log asynchronously, but if the logging queue overfills
     * the application will fallback to synchronous logging,
     */
    ASYNC_STALLING(true, false, false),

    /**
     * The appender will make best efforts to log asynchronously, but if the logging queue overfills
     * the application will drop incoming values of TRACE, DEBUG and INFO.
     */
    ASYNC_DROPPING(true, true, false),

    /**
     * The appender will make best efforts to log asynchronously, but if the logging queue overfills
     * the application will fallback to synchronous logging, this policy will aim to preserve full
     * caller information which can be expensive.
     */
    ASYNC_STALLING_FULL_CALLEE(true, false, true),

    /**
     * The appender will make best efforts to log asynchronously, but if the logging queue overfills
     * the application will drop incoming values of TRACE, DEBUG and INFO, this policy will aim to
     * preserve full caller information which can be expensive.
     */
    ASYNC_DROPPING_FULL_CALLEE(true, true, true);

    public final boolean async;
    public final boolean calleeData;
    public final boolean discarding;

    AppenderPolicy(boolean async, boolean discarding, boolean calleeData) {
        this.async = async;
        this.discarding = discarding;
        this.calleeData = calleeData;
    }

    public Appender<ILoggingEvent> wrapAppenderIfNecessary(Appender<ILoggingEvent> delegate, int queueSize) {
        if (this.async) {
            AsyncAppender asyncAppender = new AsyncAppender();
            asyncAppender.addAppender(delegate);
            asyncAppender.setQueueSize(queueSize);
            asyncAppender.setDiscardingThreshold(this.discarding ? 5 : 0);
            asyncAppender.setIncludeCallerData(this.calleeData);
            return asyncAppender;
        } else {
            return delegate;
        }
    }
}
