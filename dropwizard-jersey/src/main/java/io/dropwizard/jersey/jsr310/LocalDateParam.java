package io.dropwizard.jersey.jsr310;

import io.dropwizard.jersey.params.AbstractParam;

import javax.annotation.Nullable;
import java.time.LocalDate;

/**
 * A parameter encapsulating date values. All non-parsable values will return a {@code 400 Bad
 * Request} response.
 *
 * @see LocalDate
 */
public class LocalDateParam extends AbstractParam<LocalDate> {
    public LocalDateParam(@Nullable final String input) {
        super(input);
    }

    @Override
    protected LocalDate parse(@Nullable final String input) throws Exception {
        return LocalDate.parse(input);
    }
}
