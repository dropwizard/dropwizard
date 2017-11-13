package io.dropwizard.jersey.jsr310;

import io.dropwizard.jersey.params.AbstractParam;

import javax.annotation.Nullable;
import java.time.OffsetDateTime;

/**
 * A parameter encapsulating date/time values containing an offset from UTC.
 * All non-parsable values will return a {@code 400 Bad Request} response.
 *
 * @see OffsetDateTime
 */
public class OffsetDateTimeParam extends AbstractParam<OffsetDateTime> {
    public OffsetDateTimeParam(@Nullable final String input) {
        super(input);
    }

    @Override
    protected OffsetDateTime parse(@Nullable final String input) throws Exception {
        return OffsetDateTime.parse(input);
    }
}
