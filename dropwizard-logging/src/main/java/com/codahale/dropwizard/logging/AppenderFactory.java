package com.codahale.dropwizard.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Layout;
import com.codahale.dropwizard.jackson.Discoverable;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * A service provider interface for creating Logback {@link Appender} instances.
 * <p/>
 * To create your own, just:
 * <ol>
 * <li>Create a class which implements {@link AppenderFactory}.</li>
 * <li>Annotate it with {@code @JsonTypeName} and give it a unique type name.</li>
 * <li>add a {@code META-INF/services/com.codahale.dropwizard.logging.AppenderFactory} file with your
 * implementation's full class name to the class path.</li>
 * </ol>
 *
 * @see ConsoleAppenderFactory
 * @see FileAppenderFactory
 * @see SyslogAppenderFactory
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public interface AppenderFactory extends Discoverable {
    /**
     * Given a Logback context, an application name, and a layout, build a new appender.
     *
     * @param context         the Logback context
     * @param applicationName the application name
     * @param layout          the layout for logging
     * @return a new, started {@link Appender}
     */
    Appender<ILoggingEvent> build(LoggerContext context,
                                  String applicationName,
                                  Layout<ILoggingEvent> layout);
}
