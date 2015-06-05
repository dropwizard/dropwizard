package io.dropwizard.jersey.params;

/**
 * A parameter encapsulating boolean values. If the query parameter value is {@code "true"},
 * regardless of case, the returned value is {@link Boolean#TRUE}. If the query parameter value is
 * {@code "false"}, regardless of case, the returned value is {@link Boolean#FALSE}. All other
 * values will return a {@code 400 Bad Request} response.
 */
public class BooleanParam extends AbstractParam<Boolean> {
    public BooleanParam(String input) {
        super(input);
    }

    @Override
    protected String errorMessage(Exception e) {
        return "Parameter must be \"true\" or \"false\".";
    }

    @Override
    protected Boolean parse(String input) throws Exception {
        if ("true".equalsIgnoreCase(input)) {
            return Boolean.TRUE;
        }
        if ("false".equalsIgnoreCase(input)) {
            return Boolean.FALSE;
        }
        throw new Exception();
    }
}
