package io.dropwizard.logging.layout;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.contrib.jackson.JacksonJsonFormatter;
import ch.qos.logback.contrib.json.JsonLayoutBase;
import ch.qos.logback.core.pattern.PatternLayoutBase;
import io.dropwizard.logging.DropwizardLayout;
import io.dropwizard.logging.JsonStacktraceLayout;

import java.util.TimeZone;

/**
 * Factory that creates a {@link DropwizardLayout}
 */
public class DropwizardLayoutFactory implements LayoutFactory<ILoggingEvent> {
    @Override
    public PatternLayoutBase<ILoggingEvent> buildPatternLayout(LoggerContext context, TimeZone timeZone) {
        return new DropwizardLayout(context, timeZone);
    }

    @Override
    public JsonLayoutBase<ILoggingEvent> buildJsonLayout(LoggerContext context, TimeZone timeZone,
                                                         String timestampFormat, boolean includeStackTrace,
                                                         boolean prettyPrint) {
        JacksonJsonFormatter jsonFormatter = new JacksonJsonFormatter();
        jsonFormatter.setPrettyPrint(prettyPrint);
        JsonStacktraceLayout jsonLayout = new JsonStacktraceLayout();
        jsonLayout.setJsonFormatter(jsonFormatter);
        jsonLayout.setIncludeStacktrace(includeStackTrace);
        jsonLayout.setContext(context);
        jsonLayout.setTimestampFormat(timestampFormat);
        jsonLayout.setTimestampFormatTimezoneId(timeZone.getID());
        return  jsonLayout;
    }
}
