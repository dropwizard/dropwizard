package com.yammer.dropwizard.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

import java.util.TimeZone;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
public interface LoggingOutput {
    public static final TimeZone UTC = TimeZone.getTimeZone("UTC");

    Appender<ILoggingEvent> build(LoggerContext context, String serviceName);
}
