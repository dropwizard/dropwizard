package io.dropwizard.logging;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.spi.DeferredProcessingAware;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.dropwizard.jackson.Discoverable;
import io.dropwizard.logging.async.AsyncAppenderFactory;
import io.dropwizard.logging.filter.LevelFilterFactory;
import io.dropwizard.logging.layout.LayoutFactory;

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
public interface AppenderFactory<E extends DeferredProcessingAware> extends Discoverable {
    /**
     * Given a Logback context, an application name, a layout,
     * a levelFilterFactory, and an asyncAppenderFactory build a new appender.
     *
     * @param context         the Logback context
     * @param applicationName the application name
     * @param layoutFactory   the factory for the layout for logging
     * @param levelFilterFactory the factory for the level filter
     * @param asyncAppenderFactory   the factory for the async appender
     * @return a new, started {@link Appender}
     */
    Appender<E> build(LoggerContext context,
                                  String applicationName,
                                  LayoutFactory<E> layoutFactory,
                                  LevelFilterFactory<E> levelFilterFactory,
                                  AsyncAppenderFactory<E> asyncAppenderFactory);
}
