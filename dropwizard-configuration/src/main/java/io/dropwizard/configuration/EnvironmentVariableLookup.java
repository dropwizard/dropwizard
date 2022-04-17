package io.dropwizard.configuration;

import org.apache.commons.text.lookup.StringLookup;

/**
 * A custom {@link StringLookup} implementation using environment variables as lookup source.
 *
 * @deprecated Use a method reference to @link{System#getenv()} directly instead.
 */

@Deprecated
public class EnvironmentVariableLookup implements StringLookup {
    /**
     * {@inheritDoc}
     */
    @Override
    public String lookup(String key) {
        return System.getenv(key);
    }
}
