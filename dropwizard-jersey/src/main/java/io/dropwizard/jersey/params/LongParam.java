package io.dropwizard.jersey.params;

import javax.annotation.Nullable;

/**
 * A parameter encapsulating long values. All non-decimal values will return a {@code 400 Bad
 * Request} response.
 * 
 * @deprecated As of release 1.3.9, will be removed in 2.0.0. Please use {@link java.util.OptionalLong} instead.
 */
@Deprecated
public class LongParam extends AbstractParam<Long> {
    public LongParam(@Nullable String input) {
        super(input);
    }

    public LongParam(@Nullable String input, String parameterName) {
        super(input, parameterName);
    }

    @Override
    protected String errorMessage(Exception e) {
        return "%s is not a number.";
    }

    @Override
    protected Long parse(@Nullable String input) {
        return Long.valueOf(input);
    }
}
