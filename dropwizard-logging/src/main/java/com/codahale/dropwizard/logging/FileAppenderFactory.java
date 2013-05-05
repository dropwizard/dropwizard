package com.codahale.dropwizard.logging;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.filter.ThresholdFilter;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.FileAppender;
import ch.qos.logback.core.Layout;
import ch.qos.logback.core.rolling.DefaultTimeBasedFileNamingAndTriggeringPolicy;
import ch.qos.logback.core.rolling.RollingFileAppender;
import ch.qos.logback.core.rolling.TimeBasedRollingPolicy;
import ch.qos.logback.core.spi.FilterAttachable;
import com.codahale.dropwizard.validation.ValidationMethod;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import com.google.common.base.Strings;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.TimeZone;

@JsonTypeName("file")
public class FileAppenderFactory implements AppenderFactory {
    @NotNull
    @JsonProperty
    private Level threshold = Level.ALL;

    @NotNull
    @JsonProperty
    private String currentLogFilename;

    @JsonProperty
    private boolean archive = true;

    @JsonProperty
    private String archivedLogFilenamePattern;

    @Min(1)
    @Max(50)
    @JsonProperty
    private int archivedFileCount = 5;

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

    public String getCurrentLogFilename() {
        return currentLogFilename;
    }

    public void setCurrentLogFilename(String currentLogFilename) {
        this.currentLogFilename = currentLogFilename;
    }

    public boolean isArchive() {
        return archive;
    }

    public void setArchive(boolean archive) {
        this.archive = archive;
    }

    public String getArchivedLogFilenamePattern() {
        return archivedLogFilenamePattern;
    }

    public void setArchivedLogFilenamePattern(String archivedLogFilenamePattern) {
        this.archivedLogFilenamePattern = archivedLogFilenamePattern;
    }

    public int getArchivedFileCount() {
        return archivedFileCount;
    }

    public void setArchivedFileCount(int archivedFileCount) {
        this.archivedFileCount = archivedFileCount;
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

    @ValidationMethod(message = "must have archivedLogFilenamePattern if archive is true")
    public boolean isValidArchiveConfiguration() {
        return !archive || (archivedLogFilenamePattern != null);
    }

    @Override
    public Appender<ILoggingEvent> build(LoggerContext context, String serviceName, Layout<ILoggingEvent> layout) {
                final FileAppender<ILoggingEvent> appender = archive ?
                new RollingFileAppender<ILoggingEvent>() :
                new FileAppender<ILoggingEvent>();

        appender.setAppend(true);
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

        appender.setFile(currentLogFilename);
        appender.setPrudent(false);

        addThresholdFilter(appender, threshold);

        if (archive) {
            final DefaultTimeBasedFileNamingAndTriggeringPolicy<ILoggingEvent> triggeringPolicy =
                    new DefaultTimeBasedFileNamingAndTriggeringPolicy<>();
            triggeringPolicy.setContext(context);

            final TimeBasedRollingPolicy<ILoggingEvent> rollingPolicy = new TimeBasedRollingPolicy<>();
            rollingPolicy.setContext(context);
            rollingPolicy.setFileNamePattern(archivedLogFilenamePattern);
            rollingPolicy.setTimeBasedFileNamingAndTriggeringPolicy(
                    triggeringPolicy);
            triggeringPolicy.setTimeBasedRollingPolicy(rollingPolicy);
            rollingPolicy.setMaxHistory(archivedFileCount);

            ((RollingFileAppender<ILoggingEvent>) appender).setRollingPolicy(rollingPolicy);
            ((RollingFileAppender<ILoggingEvent>) appender).setTriggeringPolicy(triggeringPolicy);

            rollingPolicy.setParent(appender);
            rollingPolicy.start();
        }

        appender.stop();
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
