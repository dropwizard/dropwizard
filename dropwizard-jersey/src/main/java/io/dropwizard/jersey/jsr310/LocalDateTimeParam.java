package io.dropwizard.jersey.jsr310;

import io.dropwizard.jersey.params.AbstractParam;

import javax.annotation.Nullable;
import java.time.LocalDateTime;

/**
 * A parameter encapsulating date/time values. All non-parsable values will return a {@code 400 Bad
 * Request} response.
 *
 * @see LocalDateTime
 */
public class LocalDateTimeParam extends AbstractParam<LocalDateTime> {
    public LocalDateTimeParam(@Nullable final String input) {
        super(input);
    }

    @Override
    protected LocalDateTime parse(@Nullable final String input) throws Exception {
        return LocalDateTime.parse(input);
    }
}
