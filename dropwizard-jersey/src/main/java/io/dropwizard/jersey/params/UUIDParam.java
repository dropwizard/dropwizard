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

    @Override
    protected String errorMessage(Exception e) {
        return "Parameter is not a UUID.";
    }

    @Override
    protected UUID parse(String input) throws Exception {
        return UUID.fromString(input);
    }

}
