package com.yammer.dropwizard.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.util.TimeZone;

import static com.yammer.dropwizard.config.LoggingConfiguration.*;

@SuppressWarnings("UnusedDeclaration")
public class RequestLogConfiguration {
    @NotNull
    @JsonProperty("console")
    private ConsoleConfiguration consoleConfiguration = new ConsoleConfiguration();

    @NotNull
    @JsonProperty("file")
    private FileConfiguration fileConfiguration = new FileConfiguration();

    @NotNull
    @JsonProperty("syslog")
    private SyslogConfiguration syslogConfiguration = new SyslogConfiguration();

    @NotNull
    @JsonProperty
    private TimeZone timeZone = UTC;

    public ConsoleConfiguration getConsoleConfiguration() {
        return consoleConfiguration;
    }

    public void setConsoleConfiguration(ConsoleConfiguration consoleConfiguration) {
        this.consoleConfiguration = consoleConfiguration;
    }

    public FileConfiguration getFileConfiguration() {
        return fileConfiguration;
    }

    public void setFileConfiguration(FileConfiguration fileConfiguration) {
        this.fileConfiguration = fileConfiguration;
    }

    public SyslogConfiguration getSyslogConfiguration() {
        return syslogConfiguration;
    }

    public void setSyslogConfiguration(SyslogConfiguration syslogConfiguration) {
        this.syslogConfiguration = syslogConfiguration;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }

    public void setTimeZone(TimeZone timeZone) {
        this.timeZone = timeZone;
    }
}
