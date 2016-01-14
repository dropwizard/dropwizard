package io.dropwizard.jetty;

import ch.qos.logback.access.PatternLayout;
import ch.qos.logback.core.Context;

/**
 * A base layout for Dropwizard request logs.
 * <ul>
 * <li>Disables pattern headers.</li>
 * </ul>
 */
public class DropwizardRequestLayout extends PatternLayout {

    public DropwizardRequestLayout(Context context, String pattern) {
        setOutputPatternAsHeader(false);

        setPattern(pattern);
        setContext(context);
    }
}
