package io.dropwizard.jersey.params;

/**
 * A parameter encapsulating long values. All non-decimal values will return a {@code 400 Bad
 * Request} response.
 */
public class LongParam extends AbstractParam<Long> {
    public LongParam(String input) {
        super(input);
    }

    public LongParam(String input, String parameterName) {
        super(input, parameterName);
    }

    @Override
    protected String errorMessage(Exception e) {
        return "%s is not a number.";
    }

    @Override
    protected Long parse(String input) {
        return Long.valueOf(input);
    }
}
