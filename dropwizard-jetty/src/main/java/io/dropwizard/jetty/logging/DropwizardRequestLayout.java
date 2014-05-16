package io.dropwizard.jetty.logging;

import ch.qos.logback.access.PatternLayout;
import ch.qos.logback.core.Context;

public class DropwizardRequestLayout extends PatternLayout {

    public DropwizardRequestLayout(Context context, String pattern) {
        setOutputPatternAsHeader(false);

        setPattern(pattern);
        setContext(context);
    }
}
