package io.dropwizard.jersey.params;

import io.dropwizard.util.Duration;

/**
 * A parameter encapsulating duration values. All non-parsable values will return a {@code 400 Bad
 * Request} response. Supports all input formats the {@link Duration} class supports.
 */
public class DurationParam extends AbstractParam<Duration> {

    public DurationParam(String input) {
        super(input);
    }

    public DurationParam(String input, String parameterName) {
        super(input, parameterName);
    }

    @Override
    protected String errorMessage(Exception e) {
        return "%s is not a valid duration.";
    }

    @Override
    protected Duration parse(String input) throws Exception {
        return Duration.parse(input);
    }

}
