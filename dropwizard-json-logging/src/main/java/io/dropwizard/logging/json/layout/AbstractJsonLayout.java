package io.dropwizard.logging.json.layout;

import ch.qos.logback.core.LayoutBase;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * Provides the common functionality for building JSON representations
 * of {@link ch.qos.logback.access.spi.IAccessEvent} and {@link ch.qos.logback.classic.spi.ILoggingEvent}
 * events.
 *
 * @param <E> represents the type of the event
 */
public abstract class AbstractJsonLayout<E> extends LayoutBase<E> {

    private final JsonFormatter jsonFormatter;

    protected AbstractJsonLayout(JsonFormatter jsonFormatter) {
        this.jsonFormatter = jsonFormatter;
    }

    @Override
    @Nullable
    public String doLayout(E event) {
        return jsonFormatter.toJson(toJsonMap(event));
    }

    /**
     * Converts the provided logging event to a generic {@link Map}
     */
    protected abstract Map<String, Object> toJsonMap(E event);
}
