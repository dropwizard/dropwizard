package io.dropwizard.jersey.params;

import org.jspecify.annotations.Nullable;

/**
 * A parameter encapsulating integer values. All non-decimal values will return a
 * {@code 400 Bad Request} response.
 *
 * @deprecated As of release 2.0.0, will be removed in 3.0.0. Please use {@link java.util.OptionalInt} instead.
 */
@Deprecated
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
