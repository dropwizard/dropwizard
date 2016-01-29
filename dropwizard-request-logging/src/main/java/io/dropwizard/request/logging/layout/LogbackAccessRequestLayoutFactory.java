package io.dropwizard.request.logging.layout;

import java.util.TimeZone;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.core.pattern.PatternLayoutBase;
import io.dropwizard.logging.layout.LayoutFactory;

/**
 * Factory that creates a {@link LogbackAccessRequestLayout}
 */
public class LogbackAccessRequestLayoutFactory implements LayoutFactory<IAccessEvent> {
    @Override
    public PatternLayoutBase<IAccessEvent> build(LoggerContext context, TimeZone timeZone) {
        return new LogbackAccessRequestLayout(context, timeZone);
    }
}
