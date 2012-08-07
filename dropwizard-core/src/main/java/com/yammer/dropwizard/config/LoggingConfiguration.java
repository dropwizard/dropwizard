package com.yammer.dropwizard.config;

import ch.qos.logback.classic.Level;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.yammer.dropwizard.validation.ValidationMethod;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.TimeZone;

@SuppressWarnings({"FieldCanBeLocal", "FieldMayBeFinal"})
public class LoggingConfiguration {
    static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    public static class ConsoleConfiguration {
        @JsonProperty
        protected boolean enabled = true;

        @NotNull
        @JsonProperty
        protected Level threshold = Level.ALL;

        @NotNull
        @JsonProperty
        protected TimeZone timeZone = UTC;

        @JsonProperty
        protected String logFormat;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

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

        public Optional<String> getLogFormat() {
            return Optional.fromNullable(logFormat);
        }
    }

    @SuppressWarnings("CanBeFinal")
    public static class FileConfiguration {
        @JsonProperty
        protected boolean enabled = false;

        @NotNull
        @JsonProperty
        protected Level threshold = Level.ALL;

        @JsonProperty
        protected String currentLogFilename;

        @JsonProperty
        protected boolean archive = true;

        @JsonProperty
        protected String archivedLogFilenamePattern;

        @Min(1)
        @Max(50)
        @JsonProperty
        protected int archivedFileCount = 5;

        @NotNull
        @JsonProperty
        protected TimeZone timeZone = UTC;

        @JsonProperty
        protected String logFormat;

        @ValidationMethod(message = "must have logging.file.archivedLogFilenamePattern if logging.file.archive is true")
        public boolean isValidArchiveConfiguration() {
            return !enabled || !archive || archivedLogFilenamePattern != null;
        }

        @ValidationMethod(message = "must have logging.file.currentLogFilename if logging.file.enabled is true")
        public boolean isConfigured() {
            return !enabled || currentLogFilename != null;
        }

        public boolean isEnabled() {
            return enabled;
        }

        public Level getThreshold() {
            return threshold;
        }

        public String getCurrentLogFilename() {
            return currentLogFilename;
        }

        public boolean isArchive() {
            return archive;
        }

        public int getArchivedFileCount() {
            return archivedFileCount;
        }

        public String getArchivedLogFilenamePattern() {
            return archivedLogFilenamePattern;
        }

        public TimeZone getTimeZone() {
            return timeZone;
        }

        public Optional<String> getLogFormat() {
            return Optional.fromNullable(logFormat);
        }
    }

    public static class SyslogConfiguration {
        @JsonProperty
        protected boolean enabled = false;

        @NotNull
        @JsonProperty
        protected Level threshold = Level.ALL;

        @NotNull
        @JsonProperty
        protected String host = "localhost";

        @NotNull
        @JsonProperty
        @Pattern(
                regexp = "(auth|authpriv|daemon|cron|ftp|lpr|kern|mail|news|syslog|user|uucp|local[0-7])",
                message = "must be a valid syslog facility"
        )
        protected String facility = "local0";

        @NotNull
        @JsonProperty
        protected TimeZone timeZone = UTC;

        @JsonProperty
        protected String logFormat;

        public boolean isEnabled() {
            return enabled;
        }

        public Level getThreshold() {
            return threshold;
        }

        public String getHost() {
            return host;
        }

        public String getFacility() {
            return facility;
        }

        public TimeZone getTimeZone() {
            return timeZone;
        }

        public Optional<String> getLogFormat() {
            return Optional.fromNullable(logFormat);
        }
    }

    @NotNull
    @JsonProperty
    protected Level level = Level.INFO;

    @NotNull
    @JsonProperty
    protected ImmutableMap<String, Level> loggers = ImmutableMap.of();

    @Valid
    @NotNull
    @JsonProperty
    protected ConsoleConfiguration console = new ConsoleConfiguration();

    @Valid
    @NotNull
    @JsonProperty
    protected FileConfiguration file = new FileConfiguration();

    @Valid
    @NotNull
    @JsonProperty
    protected SyslogConfiguration syslog = new SyslogConfiguration();

    public Level getLevel() {
        return level;
    }

    public ImmutableMap<String, Level> getLoggers() {
        return loggers;
    }

    public ConsoleConfiguration getConsoleConfiguration() {
        return console;
    }

    public FileConfiguration getFileConfiguration() {
        return file;
    }

    public SyslogConfiguration getSyslogConfiguration() {
        return syslog;
    }
}
