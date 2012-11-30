package com.yammer.dropwizard.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.util.TimeZone;

import static com.yammer.dropwizard.config.LoggingConfiguration.*;

@SuppressWarnings("UnusedDeclaration")
public class RequestLogConfiguration {
    @NotNull
    @JsonProperty
    private ConsoleConfiguration console = new ConsoleConfiguration();

    @NotNull
    @JsonProperty
    private FileConfiguration file = new FileConfiguration();

    @NotNull
    @JsonProperty
    private SyslogConfiguration syslog = new SyslogConfiguration();

    @NotNull
    @JsonProperty
    private TimeZone timeZone = UTC;

    public ConsoleConfiguration getConsoleConfiguration() {
        return console;
    }

    public void setConsoleConfiguration(ConsoleConfiguration consoleConfiguration) {
        this.console = consoleConfiguration;
    }

    public FileConfiguration getFileConfiguration() {
        return file;
    }

    public void setFileConfiguration(FileConfiguration fileConfiguration) {
        this.file = fileConfiguration;
    }

    public SyslogConfiguration getSyslogConfiguration() {
        return syslog;
    }

    public void setSyslogConfiguration(SyslogConfiguration syslogConfiguration) {
        this.syslog = syslogConfiguration;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }
}
