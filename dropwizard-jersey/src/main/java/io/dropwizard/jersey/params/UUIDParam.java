package io.dropwizard.jersey.params;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * A parameter encapsulating UUID values. All non-parsable values will return a {@code 400 Bad
 * Request} response.
 *
 * @deprecated As of release 1.3.9, will be removed in 2.0.0.
 */
@Deprecated
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
        return UUID.fromString(input);
    }

}
