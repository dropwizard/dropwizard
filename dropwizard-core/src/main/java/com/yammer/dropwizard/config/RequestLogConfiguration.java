package com.yammer.dropwizard.config;

import org.codehaus.jackson.annotate.JsonProperty;

import javax.validation.constraints.NotNull;

import java.util.TimeZone;

import static com.yammer.dropwizard.config.LoggingConfiguration.*;

@SuppressWarnings({ "FieldMayBeFinal", "FieldCanBeLocal" })
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

    public FileConfiguration getFileConfiguration() {
        return file;
    }

    public SyslogConfiguration getSyslogConfiguration() {
        return syslog;
    }

    public TimeZone getTimeZone() {
        return timeZone;
    }
}
