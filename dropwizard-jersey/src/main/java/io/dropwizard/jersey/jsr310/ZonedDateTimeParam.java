package io.dropwizard.jersey.jsr310;

import io.dropwizard.jersey.params.AbstractParam;

import javax.annotation.Nullable;
import java.time.ZonedDateTime;

/**
 * A parameter encapsulating date/time values containing timezone information.
 * All non-parsable values will return a {@code 400 Bad Request} response.
 *
 * @see ZonedDateTime
 */
public class ZonedDateTimeParam extends AbstractParam<ZonedDateTime> {
    public ZonedDateTimeParam(@Nullable final String input) {
        super(input);
    }

    @Override
    protected ZonedDateTime parse(@Nullable final String input) throws Exception {
        return ZonedDateTime.parse(input);
    }
}
