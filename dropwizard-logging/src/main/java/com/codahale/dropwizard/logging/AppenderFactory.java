package com.codahale.dropwizard.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Layout;
import com.codahale.dropwizard.util.Subtyped;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
public interface AppenderFactory extends Subtyped {
    Appender<ILoggingEvent> build(LoggerContext context,
                                  String applicationName,
                                  Layout<ILoggingEvent> layout);
}
