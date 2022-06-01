package io.dropwizard.jersey.jsr310;

import io.dropwizard.jersey.params.AbstractParam;
import java.time.Instant;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * A parameter encapsulating instant values in seconds. All non-parsable values
 * will return a {@code 400 Bad Request} response.
 *
 * @see Instant
 */
public class InstantSecondParam extends AbstractParam<Instant> {
    public InstantSecondParam(@Nullable final String input) {
        super(input);
    }

    @Override
    protected Instant parse(@Nullable final String input) throws Exception {
        return Instant.ofEpochSecond(Long.parseLong(input));
    }
}
