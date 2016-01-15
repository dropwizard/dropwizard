package io.dropwizard.jetty;

import ch.qos.logback.access.PatternLayout;
import ch.qos.logback.core.Context;

/**
 * A base layout for Logback Access request logs.
 * <ul>
 *     <li>Extends {@link PatternLayout}.</li>
 *     <li>Disables pattern headers.</li>
 * </ul>
 */
public class LogbackAccessRequestLayout extends PatternLayout {

    public LogbackAccessRequestLayout(Context context, String pattern) {
        setOutputPatternAsHeader(false);

        setPattern(pattern);
        setContext(context);
    }
}
