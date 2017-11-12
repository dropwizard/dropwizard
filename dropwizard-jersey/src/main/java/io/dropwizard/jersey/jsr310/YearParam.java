package io.dropwizard.jersey.jsr310;

import io.dropwizard.jersey.params.AbstractParam;

import javax.annotation.Nullable;
import java.time.Year;

/**
 * A parameter encapsulating year values. All non-parsable values will return a {@code 400 Bad
 * Request} response.
 *
 * @see java.time.YearMonth
 */
public class YearParam extends AbstractParam<Year> {
    public YearParam(@Nullable final String input) {
        super(input);
    }

    @Override
    protected Year parse(@Nullable final String input) throws Exception {
        return Year.parse(input);
    }
}
