package com.codahale.dropwizard.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.ConsoleAppender;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.spi.FilterAttachable;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.fasterxml.jackson.annotation.JsonValue;
import com.google.common.base.Strings;

import javax.validation.constraints.NotNull;
import java.util.Locale;
import java.util.TimeZone;

@JsonTypeName("console")
public class ConsoleAppenderFactory implements AppenderFactory {
    public enum OutputTarget {
        STDERR("System.err"),
        STDOUT("System.out");

        private final String logbackName;

        private OutputTarget(String logbackName) {
            this.logbackName = logbackName;
        }

        @JsonCreator
        public static OutputTarget parse(String s) {
            return valueOf(s.toUpperCase(Locale.US));
        }

        @Override
        @JsonValue
        public String toString() {
            return super.toString().toLowerCase(Locale.US);
        }

        public String getLogbackName() {
            return logbackName;
        }
    }

    @NotNull
    private Level threshold = Level.ALL;

    @NotNull
    private TimeZone timeZone = TimeZone.getTimeZone("UTC");

    @NotNull
    private OutputTarget target = OutputTarget.STDOUT;

    private String logFormat;

    @JsonProperty
    public Level getThreshold() {
        return threshold;
    }

    @JsonProperty
    public void setThreshold(Level threshold) {
        this.threshold = threshold;
    }

    @JsonProperty
    public TimeZone getTimeZone() {
        return timeZone;
    }

    @JsonProperty
    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }

    @JsonProperty
    public String getLogFormat() {
        return logFormat;
    }

    @JsonProperty
    public void setLogFormat(String logFormat) {
        this.logFormat = logFormat;
    }

    @Override
    public Appender<ILoggingEvent> build(LoggerContext context, String applicationName, Layout<ILoggingEvent> layout) {
        final ConsoleAppender<ILoggingEvent> appender = new ConsoleAppender<>();
        appender.setContext(context);
        appender.setTarget(target.getLogbackName());
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
