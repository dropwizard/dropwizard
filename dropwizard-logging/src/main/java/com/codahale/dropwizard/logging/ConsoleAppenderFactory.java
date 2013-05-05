package com.codahale.dropwizard.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.spi.FilterAttachable;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.base.Strings;

import javax.validation.constraints.NotNull;
import java.util.TimeZone;

@JsonTypeName("console")
public class ConsoleAppenderFactory implements AppenderFactory {
    @NotNull
    @JsonProperty
    private Level threshold = Level.ALL;

    @NotNull
    @JsonProperty
    private TimeZone timeZone = TimeZone.getTimeZone("UTC");

    @JsonProperty
    private String logFormat;

    public Level getThreshold() {
        return threshold;
    }

    public void setThreshold(Level threshold) {
        this.threshold = threshold;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    public String getLogFormat() {
        return logFormat;
    }

    public void setLogFormat(String logFormat) {
        this.logFormat = logFormat;
    }

    @Override
    public Appender<ILoggingEvent> build(LoggerContext context, String serviceName, Layout<ILoggingEvent> layout) {
        final ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<>();
        appender.setContext(context);
        if (layout == null) {
            final LogFormatter formatter = new LogFormatter(context, timeZone);
            if (!Strings.isNullOrEmpty(logFormat)) {
                formatter.setPattern(logFormat);
            }
            formatter.start();
            appender.setLayout(formatter);
        } else {
            appender.setLayout(layout);
        }

        addThresholdFilter(appender, threshold);
        appender.start();

        return appender;
    }

    private void addThresholdFilter(FilterAttachable<ILoggingEvent> appender, Level threshold) {
        final ThresholdFilter filter = new ThresholdFilter();
        filter.setLevel(threshold.toString());
        filter.start();
        appender.addFilter(filter);
    }
}
