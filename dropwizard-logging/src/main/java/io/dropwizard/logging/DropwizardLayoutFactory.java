package io.dropwizard.logging;

import java.util.TimeZone;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.pattern.PatternLayoutBase;

/**
 * Factory that creates a {@link DropwizardLayout}
 */
public class DropwizardLayoutFactory implements LayoutFactory<ILoggingEvent> {
    @Override
    public PatternLayoutBase<ILoggingEvent> build(LoggerContext context, TimeZone timeZone) {
        return new DropwizardLayout(context, timeZone);
    }
}
