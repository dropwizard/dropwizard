package io.dropwizard.jersey.jsr310;

import io.dropwizard.jersey.params.AbstractParam;

import java.time.LocalDate;

/**
 * A parameter encapsulating date values. All non-parsable values will return a {@code 400 Bad
 * Request} response.
 *
 * @see LocalDate
 */
public class LocalDateParam extends AbstractParam<LocalDate> {
    public LocalDateParam(final String input) {
        super(input);
    }

    @Override
    protected LocalDate parse(final String input) throws Exception {
        return LocalDate.parse(input);
    }
}
