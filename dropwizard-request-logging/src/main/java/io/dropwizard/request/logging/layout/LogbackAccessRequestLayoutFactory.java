package io.dropwizard.request.logging.layout;

import ch.qos.logback.access.spi.IAccessEvent;
import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.contrib.json.JsonLayoutBase;
import ch.qos.logback.core.pattern.PatternLayoutBase;
import io.dropwizard.logging.layout.LayoutFactory;

import java.util.TimeZone;

/**
 * Factory that creates a {@link LogbackAccessRequestLayout}
 */
public class LogbackAccessRequestLayoutFactory implements LayoutFactory<IAccessEvent> {
    @Override
    public PatternLayoutBase<IAccessEvent> buildPatternLayout(LoggerContext context, TimeZone timeZone) {
        return new LogbackAccessRequestLayout(context, timeZone);
    }

    @Override
    public JsonLayoutBase<IAccessEvent> buildJsonLayout(LoggerContext context, TimeZone timeZone,
                                                        String timestampFormat,
                                                        boolean includeStackTrace, boolean prettyPrint) {
        return new LogbackAccessRequestJsonLayout(context, timeZone, timestampFormat);
    }


}
