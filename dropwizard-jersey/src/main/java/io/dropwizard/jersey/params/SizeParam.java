package io.dropwizard.jersey.params;

import io.dropwizard.util.Size;

/**
 * A parameter encapsulating size values. All non-parsable values will return a {@code 400 Bad
 * Request} response. Supports all input formats the {@link Size} class supports.
 */
public class SizeParam extends AbstractParam<Size> {

    public SizeParam(String input) {
        super(input);
    }

    public SizeParam(String input, String parameterName) {
        super(input, parameterName);
    }

    @Override
    protected String errorMessage(Exception e) {
        return "%s is not a valid size.";
    }

    @Override
    protected Size parse(String input) throws Exception {
        return Size.parse(input);
    }
}
