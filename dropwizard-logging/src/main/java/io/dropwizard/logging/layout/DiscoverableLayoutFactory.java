package io.dropwizard.logging.layout;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.LayoutBase;
import ch.qos.logback.core.spi.DeferredProcessingAware;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.dropwizard.jackson.Discoverable;

import java.util.TimeZone;

/**
 * An interface for building Logback {@link LayoutBase} layouts, which could be discovered by Jackson
 * and specified in the logging configuration.
 *
 * @param <E> The type of log event
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
public interface DiscoverableLayoutFactory<E extends DeferredProcessingAware> extends Discoverable {

    /**
     * Creates a {@link LayoutBase} of type E
     *
     * @param context  the Logback context
     * @param timeZone the TimeZone
     * @return a new {@link LayoutBase}
     */
    LayoutBase<E> build(LoggerContext context, TimeZone timeZone);
}
