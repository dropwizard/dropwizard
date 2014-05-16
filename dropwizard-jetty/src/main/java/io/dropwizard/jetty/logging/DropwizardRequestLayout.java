package io.dropwizard.jetty.logging;

import ch.qos.logback.access.PatternLayout;
import ch.qos.logback.core.Context;

import java.util.TimeZone;

public class DropwizardRequestLayout extends PatternLayout {

    public DropwizardRequestLayout(Context context, TimeZone timeZone) {
        setOutputPatternAsHeader(false);

        setPattern("HTTP  [%t{ISO8601," + timeZone.getID() + "}] %h %l %u \"%r\" %s %b \"%i{Referer}\" \"%i{User-Agent}\"");
        setContext(context);
    }
}
