package io.dropwizard.jersey.params;

import javax.annotation.Nullable;

/**
 * A parameter encapsulating integer values. All non-decimal values will return a
 * {@code 400 Bad Request} response.
 */
public class IntParam extends AbstractParam<Integer> {
    public IntParam(@Nullable String input) {
        super(input);
    }

    public IntParam(@Nullable String input, String parameterName) {
        super(input, parameterName);
    }

    @Override
    protected String errorMessage(Exception e) {
        return "%s is not a number.";
    }

    @Override
    protected Integer parse(@Nullable String input) {
        return Integer.valueOf(input);
    }
}
