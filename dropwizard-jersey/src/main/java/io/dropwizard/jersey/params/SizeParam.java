package io.dropwizard.jersey.params;

import io.dropwizard.util.Size;

import javax.annotation.Nullable;

import static java.util.Objects.requireNonNull;

/**
 * A parameter encapsulating size values. All non-parsable values will return a {@code 400 Bad
 * Request} response. Supports all input formats the {@link Size} class supports.
 */
public class SizeParam extends AbstractParam<Size> {

    public SizeParam(@Nullable String input) {
        super(input);
    }

    public SizeParam(@Nullable String input, String parameterName) {
        super(input, parameterName);
    }

    @Override
    protected String errorMessage(Exception e) {
        return "%s is not a valid size.";
    }

    @Override
    protected Size parse(@Nullable String input) throws Exception {
        return Size.parse(requireNonNull(input));
    }
}
