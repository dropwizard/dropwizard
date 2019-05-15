package io.dropwizard.request.logging.old;

import java.util.TimeZone;

public class ClassicLogFormat {
    public static String pattern() {
        return pattern(TimeZone.getTimeZone("UTC"));
    }

    public static String pattern(TimeZone tz) {
        // Format string adapted from `LogbackAccessRequestLayout`.
        // Previously Jetty had the pattern hardcoded in a class called
        // `AbstractNCSARequestLog`, and this is the best approximation
        // of that class
        return "%{client}a - %u %{dd/MMM/yyyy:HH:mm:ss Z|" + tz.getID()
            + "}t \"%m %U %H\" %s %O \"%{Referer}i\" \"%{User-Agent}i\" %{ms}T";
    }
}
