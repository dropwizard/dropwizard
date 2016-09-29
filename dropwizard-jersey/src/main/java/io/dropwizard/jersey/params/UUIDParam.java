package io.dropwizard.jersey.params;

import java.util.UUID;

/**
 * A parameter encapsulating UUID values. All non-parsable values will return a {@code 400 Bad
 * Request} response.
 */
public class UUIDParam extends AbstractParam<UUID> {

    public UUIDParam(String input) {
        super(input);
    }

    public UUIDParam(String input, String parameterName) {
        super(input, parameterName);
    }

    @Override
    protected String errorMessage(Exception e) {
        return "%s is not a UUID.";
    }

    @Override
    protected UUID parse(String input) throws Exception {
        return UUID.fromString(input);
    }

}
