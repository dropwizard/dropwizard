package com.yammer.dropwizard.config;

import com.google.common.collect.ImmutableMap;
import com.yammer.dropwizard.util.Size;
import org.apache.log4j.Level;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Map;

@SuppressWarnings("FieldCanBeLocal")
public class LoggingConfiguration {
    private static final String VALID_LEVEL = "(OFF|FATAL|ERROR|WARN|INFO|DEBUG|TRACE|ALL)";

    public static class ConsoleConfiguration {
        private boolean enabled = true;

        @NotNull
        @Pattern(regexp = VALID_LEVEL, flags = {Pattern.Flag.CASE_INSENSITIVE})
        private String threshold = "ALL";

        public boolean isEnabled() {
            return enabled;
        }

        public Level getThreshold() {
            return Level.toLevel(threshold);
        }

    }

    public static class FileConfiguration {
        private boolean enabled = false;

        @NotNull
        @Pattern(regexp = VALID_LEVEL, flags = {Pattern.Flag.CASE_INSENSITIVE})
        private String threshold = "ALL";

        @NotNull
        private String filenamePattern = "./logs/example.log";

        @NotNull
        @Pattern(regexp = Size.VALID_SIZE)
        private String maxFileSize = "50MB";

        @Min(1)
        @Max(50)
        private int retainedFileCount = 5;

        public boolean isEnabled() {
            return enabled;
        }

        public Level getThreshold() {
            return Level.toLevel(threshold);
        }

        public String getFilenamePattern() {
            return filenamePattern;
        }

        public Size getMaxFileSize() {
            return Size.parse(maxFileSize);
        }

        public int getRetainedFileCount() {
            return retainedFileCount;
        }
    }

    public static class SyslogConfiguration {
        private boolean enabled = true;

        @NotNull
        @Pattern(regexp = VALID_LEVEL, flags = {Pattern.Flag.CASE_INSENSITIVE})
        private String threshold = "ALL";

        @NotNull
        private String host = "localhost";

        @Pattern(regexp = "auth|authpriv|daemon|cron|ftp|lpr|kern|mail|news|syslog|user|uucp|local[0-7]]")
        @NotNull
        private String facility = "local0";

        public boolean isEnabled() {
            return enabled;
        }

        public Level getThreshold() {
            return Level.toLevel(threshold);
        }

        public String getHost() {
            return host;
        }

        public String getFacility() {
            return facility;
        }
    }

    @NotNull
    @Pattern(regexp = VALID_LEVEL, flags = {Pattern.Flag.CASE_INSENSITIVE})
    private String level = "INFO";

    @NotNull
    // TODO: 11/7/11 <coda> -- figure out how to validate these values
    private Map<String, String> loggers = ImmutableMap.of();

    @NotNull
    private ConsoleConfiguration console = new ConsoleConfiguration();

    @NotNull
    private FileConfiguration file = new FileConfiguration();

    @NotNull
    private SyslogConfiguration syslog = new SyslogConfiguration();

    public Level getLevel() {
        return Level.toLevel(level);
    }

    public ImmutableMap<String, Level> getLoggers() {
        final ImmutableMap.Builder<String, Level> loggers = ImmutableMap.builder();
        for (Map.Entry<String, String> entry : this.loggers.entrySet()) {
            loggers.put(entry.getKey(), Level.toLevel(entry.getValue()));
        }
        return loggers.build();
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
