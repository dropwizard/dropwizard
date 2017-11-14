package io.dropwizard.jersey.params;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import javax.annotation.Nullable;

/**
 * A parameter encapsulating date/time values. All non-parsable values will return a {@code 400 Bad
 * Request} response. All values returned are in UTC.
 */
public class DateTimeParam extends AbstractParam<DateTime> {
    public DateTimeParam(@Nullable String input) {
        super(input);
    }

    public DateTimeParam(@Nullable String input, String parameterName) {
        super(input, parameterName);
    }

    @Override
    protected DateTime parse(@Nullable String input) throws Exception {
        return new DateTime(input, DateTimeZone.UTC);
    }
}
