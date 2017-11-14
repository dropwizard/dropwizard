package io.dropwizard.jersey.jsr310;

import io.dropwizard.jersey.params.AbstractParam;

import javax.annotation.Nullable;
import java.time.LocalTime;

/**
 * A parameter encapsulating time values. All non-parsable values will return a {@code 400 Bad
 * Request} response.
 *
 * @see LocalTime
 */
public class LocalTimeParam extends AbstractParam<LocalTime> {
    public LocalTimeParam(@Nullable final String input) {
        super(input);
    }

    @Override
    protected LocalTime parse(@Nullable final String input) throws Exception {
        return LocalTime.parse(input);
    }
}
