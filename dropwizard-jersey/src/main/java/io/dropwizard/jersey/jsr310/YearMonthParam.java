package io.dropwizard.jersey.jsr310;

import io.dropwizard.jersey.params.AbstractParam;

import javax.annotation.Nullable;
import java.time.YearMonth;

/**
 * A parameter encapsulating year and month values. All non-parsable values will return a {@code 400 Bad
 * Request} response.
 *
 * @see YearMonth
 */
public class YearMonthParam extends AbstractParam<YearMonth> {
    public YearMonthParam(@Nullable final String input) {
        super(input);
    }

    @Override
    protected YearMonth parse(@Nullable final String input) throws Exception {
        return YearMonth.parse(input);
    }
}
