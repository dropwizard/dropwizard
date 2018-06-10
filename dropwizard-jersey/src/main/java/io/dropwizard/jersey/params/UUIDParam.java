package io.dropwizard.jersey.params;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * A parameter encapsulating UUID values. All non-parsable values will return a {@code 400 Bad
 * Request} response.
 */
public class UUIDParam extends AbstractParam<UUID> {

    public UUIDParam(@Nullable String input) {
        super(input);
    }

    public UUIDParam(@Nullable String input, String parameterName) {
        super(input, parameterName);
    }

    @Override
    protected String errorMessage(Exception e) {
        return "%s is not a UUID.";
    }

    @Override
    protected UUID parse(@Nullable String input) throws Exception {
        // From UUID RFC 4122 spec, a UUID contains 32 hex digits with 4 dashes. fromString will
        // ensure that only hex exists and that there are 4 dashes, but does no length checking, so
        // the input could have additional hex digits appended and no error would be raised. Since
        // the spec clearly defines the length to be 36, we'll ensure the input conforms. Some UUID
        // implementations are lenient and allow absent dashes (thus making the total length 32),
        // but since fromString requires dashes we don't need to worry about supporting a range of
        // lengths.
        if (input != null && input.length() != 36) {
            throw new IllegalArgumentException("Expecting a UUID of 36 in length");
        }

        return UUID.fromString(input);
    }

}
