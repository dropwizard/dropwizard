package io.dropwizard.jersey.params;

import org.joda.time.LocalDate;

import javax.annotation.Nullable;

/**
 * A parameter encapsulating local date values. All non-parsable values will return a {@code 400 Bad
 * Request} response.
 *
 * @deprecated As of release 2.0.0, will be removed in 3.0.0. Please use {@link java.util.Optional} instead.
 */
@Deprecated
public class LocalDateParam extends AbstractParam<LocalDate> {
    public LocalDateParam(@Nullable String input) {
        super(input);
    }

    public LocalDateParam(@Nullable String input, String parameterName) {
        super(input, parameterName);
    }

    @Override
    protected LocalDate parse(@Nullable String input) throws Exception {
        return new LocalDate(input);
    }
}
