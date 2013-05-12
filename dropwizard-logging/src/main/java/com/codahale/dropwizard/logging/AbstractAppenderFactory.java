package com.codahale.dropwizard.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.spi.FilterAttachable;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Strings;

import javax.validation.constraints.NotNull;
import java.util.TimeZone;

/**
 * A base implementation of {@link AppenderFactory}.
 */
public abstract class AbstractAppenderFactory implements AppenderFactory {
    @NotNull
    protected Level threshold = Level.ALL;

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
}
