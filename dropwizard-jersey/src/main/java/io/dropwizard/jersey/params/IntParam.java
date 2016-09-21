package io.dropwizard.jersey.params;

/**
 * A parameter encapsulating integer values. All non-decimal values will return a
 * {@code 400 Bad Request} response.
 */
public class IntParam extends AbstractParam<Integer> {
    public IntParam(String input) {
        super(input);
    }

    public IntParam(String input, String parameterName) {
        super(input, parameterName);
    }

    @Override
    protected String errorMessage(Exception e) {
        return "%s is not a number.";
    }

    @Override
    protected Integer parse(String input) {
        return Integer.valueOf(input);
    }
}
