package io.dropwizard.logging.layout;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.pattern.PatternLayoutBase;
import ch.qos.logback.core.spi.DeferredProcessingAware;

import java.util.TimeZone;

/**
 * An interface for building Logback {@link PatternLayoutBase} layouts
 * @param <E> The type of log event
 */
public interface LayoutFactory<E extends DeferredProcessingAware> {

    /**
     * Creates a {@link PatternLayoutBase} of type E
     * @param context the Logback context
     * @param timeZone the TimeZone
     * @return a new {@link PatternLayoutBase}
     */
    PatternLayoutBase<E> build(LoggerContext context, TimeZone timeZone);
}
