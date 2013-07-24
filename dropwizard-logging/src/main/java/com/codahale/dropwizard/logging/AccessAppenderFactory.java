package com.codahale.dropwizard.logging;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.encoder.Encoder;
import com.codahale.dropwizard.jackson.Discoverable;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * A service provider interface for creating Logback {@link ch.qos.logback.core.Appender} instances.
 * <p/>
 * To create your own, just:
 * <ol>
 * <li>Create a class which implements {@link com.codahale.dropwizard.logging.AccessAppenderFactory}.</li>
 * <li>Annotate it with {@code @JsonTypeName} and give it a unique type name.</li>
 * <li>add a {@code META-INF/services/com.codahale.dropwizard.logging.AppenderFactory} file with your
 * implementation's full class name to the class path.</li>
 * </ol>
 *
 * @see com.codahale.dropwizard.logging.ConsoleAppenderFactory
 * @see com.codahale.dropwizard.logging.FileAppenderFactory
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public interface AccessAppenderFactory extends Discoverable {
    /**
     * Given a Logback context, an application name, and a layout, build a new appender.
     *
     * @param context         the Logback context
     * @param layout          the layout for logging
     * @return a new, started {@link ch.qos.logback.core.Appender}
     */
    Appender<IAccessEvent> build(LoggerContext context,
                                  Encoder<IAccessEvent> encoder);
}
