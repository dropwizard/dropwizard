package com.codahale.dropwizard.logging;

import ch.qos.logback.classic.AsyncAppender;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.spi.FilterAttachable;
import com.codahale.dropwizard.validation.ValidationMethod;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.TimeZone;

/**
 * A base implementation of {@link AppenderFactory}.
 */
public abstract class AbstractAppenderFactory implements AppenderFactory {
    @NotNull
    protected Level threshold = Level.ALL;

    protected boolean async = false;

    protected boolean calleeData = false;

    protected boolean discarding = false;

    @Min(1)
    @Max(Integer.MAX_VALUE)
    protected int asyncQueueLength = 10000;

    protected String logFormat;

    /**
     * Returns the lowest level of events to print to the console.
     */
    @JsonProperty
    public Level getThreshold() {
        return threshold;
    }

    /**
     * Sets the lowest level of events to print to the console.
     */
    @JsonProperty
    public void setThreshold(Level threshold) {
        this.threshold = threshold;
    }

    /**
     * Returns the Logback pattern with which events will be formatted.
     */
    @JsonProperty
    public String getLogFormat() {
        return logFormat;
    }

    /**
     * Sets the Logback pattern with which events will be formatted.
     *
     * @see <a href="http://logback.qos.ch/manual/layouts.html#conversionWord">the Logback documentation</a>
     */
    @JsonProperty
    public void setLogFormat(String logFormat) {
        this.logFormat = logFormat;
    }

    /**
     * Returns true if the appender is asynchronous
     */
    public boolean isAsync() {
        return async;
    }

    /**
     * Sets if the appender will be created as an async appender.
     */
    @JsonProperty
    public void setAsync(boolean async) {
        this.async = async;
    }

    /**
     * Returns true if the async appender retains full callee data
     */
    public boolean isCalleeData() {
        return calleeData;
    }

    /**
     * Sets if the appender retains full callee data, this can be expensive
     *
     * @see <a href="http://logback.qos.ch/manual/appenders.html#asyncIncludeCallerData">the Logback documentation</a>
     */
    @JsonProperty
    public void setCalleeData(boolean calleeData) {
        this.calleeData = calleeData;
    }

    /**
     * Returns true if the appender is asynchronous and, in cases where the log queue is overflowing starts dropping new
     * events of level TRACE, DEBUG and INFO
     */
    public boolean isDiscarding() {
        return discarding;
    }

    /**
     * Sets the behaviour of the appender to be asynchronous and, in cases where the log queue
     * is overflowing starts dropping new events of level TRACE, DEBUG and INFO
     */
    @JsonProperty
    public void setDiscarding(boolean discarding) {
        this.discarding = discarding;
    }

    /**
     * If the appender is an async appender, return the queue length, if not return -1
     */
    public int getAsyncQueueLength() {
        return this.async ? asyncQueueLength : -1;
    }

    /**
     * Sets the queue length for async loggers
     */
    @JsonProperty
    public void setAsyncQueueLength(int asyncQueueLength) {
        this.asyncQueueLength = asyncQueueLength;
    }

    protected Appender<ILoggingEvent> wrapAppenderAsAsyncIfNecessary(Appender<ILoggingEvent> delegate) {
        if (this.async) {
            AsyncAppender asyncAppender = new AsyncAppender();
            asyncAppender.addAppender(delegate);
            asyncAppender.setQueueSize(this.asyncQueueLength);
            asyncAppender.setDiscardingThreshold(this.discarding ? 5 : 0);
            asyncAppender.setIncludeCallerData(this.calleeData);
            return asyncAppender;
        } else {
            return delegate;
        }
    }

    protected void addThresholdFilter(FilterAttachable<ILoggingEvent> appender, Level threshold) {
        final ThresholdFilter filter = new ThresholdFilter();
        filter.setLevel(threshold.toString());
        filter.start();
        appender.addFilter(filter);
    }

    protected DropwizardLayout buildLayout(LoggerContext context, TimeZone timeZone) {
        final DropwizardLayout formatter = new DropwizardLayout(context, timeZone);
        if (!Strings.isNullOrEmpty(logFormat)) {
            formatter.setPattern(logFormat);
        }
        formatter.start();
        return formatter;
    }

    @ValidationMethod(message="Async logging parameters specified for appender, but appender not configured to be async!")
    @SuppressWarnings("UnusedDeclaration")
    private boolean isValidConfiguration() {
        if (isDiscarding() || isCalleeData()) {
            return isAsync();
        } else {
            return true;
        }
    }

}
