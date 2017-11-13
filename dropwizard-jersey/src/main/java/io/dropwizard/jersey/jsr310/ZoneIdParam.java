package io.dropwizard.jersey.jsr310;

import io.dropwizard.jersey.params.AbstractParam;

import javax.annotation.Nullable;
import java.time.ZoneId;

/**
 * A parameter encapsulating time-zone IDs, such as Europe/Paris.
 * All non-parsable values will return a {@code 400 Bad Request} response.
 *
 * @see ZoneId
 */
public class ZoneIdParam extends AbstractParam<ZoneId> {
    public ZoneIdParam(@Nullable final String input) {
        super(input);
    }

    @Override
    protected ZoneId parse(@Nullable final String input) throws Exception {
        return ZoneId.of(input);
    }
}
