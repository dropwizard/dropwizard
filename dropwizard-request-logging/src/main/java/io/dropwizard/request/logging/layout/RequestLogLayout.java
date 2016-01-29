package io.dropwizard.request.logging.layout;

import java.util.Map;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.pattern.PatternLayoutBase;

/**
 * A Layout that extends {@link PatternLayoutBase} and overrides doLayout that appends a line separator to the message.
 */
public class RequestLogLayout extends PatternLayoutBase<ILoggingEvent> {
    @Override
    public String doLayout(ILoggingEvent event) {
        return event.getFormattedMessage() + CoreConstants.LINE_SEPARATOR;
    }

    @Override
    public Map<String, String> getDefaultConverterMap() {
        return null;
    }
}
