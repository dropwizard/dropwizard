package com.yammer.dropwizard.config;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;
import java.util.TimeZone;

import static com.yammer.dropwizard.config.LoggingConfiguration.*;

@SuppressWarnings({ "FieldMayBeFinal", "FieldCanBeLocal" })
public class RequestLogConfiguration {
    @NotNull
    @JsonProperty
    protected ConsoleConfiguration console = new ConsoleConfiguration();

    @NotNull
    @JsonProperty
    protected FileConfiguration file = new FileConfiguration();

    @NotNull
    @JsonProperty
    protected SyslogConfiguration syslog = new SyslogConfiguration();

    @NotNull
    @JsonProperty
    protected TimeZone timeZone = UTC;

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
