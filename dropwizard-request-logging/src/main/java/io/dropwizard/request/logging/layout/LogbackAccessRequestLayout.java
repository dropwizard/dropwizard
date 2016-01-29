package io.dropwizard.request.logging.layout;

import java.util.TimeZone;

import ch.qos.logback.access.PatternLayout;
import ch.qos.logback.core.Context;

/**
 * A base layout for Logback Access request logs.
 * <ul>
 *     <li>Extends {@link PatternLayout}.</li>
 *     <li>Disables pattern headers.</li>
 *     <li>Sets the pattern to the given timezone.</li>
 * </ul>
 */
public class LogbackAccessRequestLayout extends PatternLayout {

    public LogbackAccessRequestLayout(Context context, TimeZone timeZone) {
        setOutputPatternAsHeader(false);
        setPattern("%h %l %u [%t{dd/MMM/yyyy:HH:mm:ss Z," + timeZone.getID()
                + "}] \"%r\" %s %b \"%i{Referer}\" \"%i{User-Agent}\" %D");
        setContext(context);
    }
}
