package com.yammer.dropwizard.config;

import ch.qos.logback.classic.Level;
import com.google.common.base.Optional;
import com.google.common.collect.ImmutableMap;
import com.yammer.dropwizard.validation.ValidationMethod;
import org.codehaus.jackson.annotate.JsonProperty;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.LinkedList;
import java.util.List;
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

        @ValidationMethod(message = "must have logging.file.currentLogFilename and " +
                "logging.file.archivedLogFilenamePattern if logging.file.enabled is true")
        public boolean isConfigured() {
            return !enabled || ((currentLogFilename != null) && (archivedLogFilenamePattern != null));
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


    public static final class SMTPConfiguration {

        @NotNull
        @JsonProperty
        protected boolean enabled = false;

        @NotNull
        @JsonProperty
        protected Level threshold = Level.ERROR;

        @NotNull
        @JsonProperty
        protected TimeZone timeZone = UTC;

        @JsonProperty
        protected String logFormat;

        @JsonProperty
        protected String username;

        @JsonProperty
        protected String password;

        @NotNull
        @JsonProperty
        protected String host = "localhost";

        @JsonProperty
        protected Integer port;

        @JsonProperty
        protected List<String> to = new LinkedList<String>();

        @JsonProperty
        protected String from;

        @NotNull
        @JsonProperty
        protected String subject = "%logger{20} - %m";

        @NotNull
        @JsonProperty
        protected boolean ssl = false;

        @NotNull
        @JsonProperty
        protected boolean startTLS = false;

        @NotNull
        @JsonProperty
        protected String charsetEncoding = "UTF-8";

        @JsonProperty
        protected String localhost;

        public boolean isEnabled() {
            return enabled;
        }

        public Level getThreshold() {
            return threshold;
        }

        public TimeZone getTimeZone() {
            return timeZone;
        }

        public Optional<String> getLogFormat() {
            return Optional.fromNullable(logFormat);
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public String getCharsetEncoding() {
            return charsetEncoding;
        }

        public String getFrom() {
            return from;
        }

        public Optional<String> getLocalhost() {
            return Optional.fromNullable(localhost);
        }

        public String getHost() {
            return host;
        }

        public Optional<Integer> getPort() {
            return Optional.fromNullable(port);
        }

        public String getSubject() {
            return subject;
        }

        public List<String> getTo() {
            return to;
        }

        public boolean getSSL() {
            return ssl;
        }

        public boolean getSTARTTLS() {
            return startTLS;
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

    @Valid
    @NotNull
    @JsonProperty
    protected SMTPConfiguration smtp = new SMTPConfiguration();

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

    public SMTPConfiguration getSMTPConfiguration() {
        return smtp;
    }
}
