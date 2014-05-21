package io.dropwizard.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.Layout;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.dropwizard.jackson.Discoverable;
import io.dropwizard.logging.filter.FilterFactory;

/**
 * A service provider interface for creating Logback {@link Appender} instances.
 * <p/>
 * To create your own, just:
 * <ol>
 * <li>Create a class which implements {@link AppenderFactory}.</li>
 * <li>Annotate it with {@code @JsonTypeName} and give it a unique type name.</li>
 * <li>add a {@code META-INF/services/io.dropwizard.logging.AppenderFactory} file with your
 * implementation's full class name to the class path.</li>
 * </ol>
 *
 * @see ConsoleAppenderFactory
 * @see FileAppenderFactory
 * @see SyslogAppenderFactory
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public interface AppenderFactory<E> extends Discoverable {
    /**
     * Given a Logback context, an application name, and a layout, build a new appender.
     *
     * @param context         the Logback context
     * @param applicationName the application name
     * @param layout          the layout for logging
     * @return a new, started {@link Appender}
     */
    Appender<E> build(LoggerContext context,
                                  String applicationName,
                                  Layout<E> layout,
                                  FilterFactory<E> thresholdFilterFactory,
                                  AsyncAppenderFactory<E> asyncAppenderFactory);
}
