package io.dropwizard.jersey.params;

import io.dropwizard.util.Duration;

import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

/**
 * A parameter encapsulating duration values. All non-parsable values will return a {@code 400 Bad
 * Request} response. Supports all input formats the {@link Duration} class supports.
 */
public class DurationParam extends AbstractParam<Duration> {

    public DurationParam(@Nullable String input) {
        super(input);
    }

    public DurationParam(@Nullable String input, String parameterName) {
        super(input, parameterName);
    }

    @Override
    protected String errorMessage(Exception e) {
        return "%s is not a valid duration.";
    }

    @Override
    protected Duration parse(@Nullable String input) throws Exception {
        return Duration.parse(requireNonNull(input));
    }

}
