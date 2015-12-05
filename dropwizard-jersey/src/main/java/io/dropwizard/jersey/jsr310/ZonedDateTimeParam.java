package io.dropwizard.jersey.jsr310;

import io.dropwizard.jersey.params.AbstractParam;

import java.time.ZonedDateTime;

/**
 * A parameter encapsulating date/time values containing timezone information.
 * All non-parsable values will return a {@code 400 Bad Request} response.
 *
 * @see ZonedDateTime
 */
public class ZonedDateTimeParam extends AbstractParam<ZonedDateTime> {
    public ZonedDateTimeParam(final String input) {
        super(input);
    }

    @Override
    protected ZonedDateTime parse(final String input) throws Exception {
        return ZonedDateTime.parse(input);
    }
}
