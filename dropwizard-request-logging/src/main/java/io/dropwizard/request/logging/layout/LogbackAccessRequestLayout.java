package io.dropwizard.request.logging.layout;

import ch.qos.logback.access.common.PatternLayout;
import ch.qos.logback.core.Context;

import java.util.TimeZone;

/**
 * A base layout for Logback Access request logs.
 * <ul>
 *     <li>Extends {@link PatternLayout}.</li>
 *     <li>Disables pattern headers.</li>
 *     <li>Sets the pattern to the given timezone.</li>
 * </ul>
 */
public class LogbackAccessRequestLayout extends PatternLayout {

    static  {
        // Replace the buggy default converter which don't work async appenders
        ch.qos.logback.classic.PatternLayout.DEFAULT_CONVERTER_MAP.put("requestParameter", SafeRequestParameterConverter.class.getName());
        ch.qos.logback.classic.PatternLayout.DEFAULT_CONVERTER_MAP.put("reqParameter", SafeRequestParameterConverter.class.getName());
    }

    public LogbackAccessRequestLayout(Context context, TimeZone timeZone) {
        setOutputPatternAsHeader(false);
        setPattern("%h %l %u [%t{dd/MMM/yyyy:HH:mm:ss Z," + timeZone.getID()
                + "}] \"%r\" %s %b \"%i{Referer}\" \"%i{User-Agent}\" %D");
        setContext(context);
    }
}
