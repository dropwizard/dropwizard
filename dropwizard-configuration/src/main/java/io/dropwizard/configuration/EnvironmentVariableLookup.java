package io.dropwizard.configuration;

import org.apache.commons.lang3.text.StrLookup;

/**
 * A custom {@link org.apache.commons.lang3.text.StrLookup} implementation using environment variables as lookup source.
 */
public class EnvironmentVariableLookup extends StrLookup<Object> {
    private final boolean strict;

    /**
     * Create a new instance with strict behavior.
     */
    public EnvironmentVariableLookup() {
        this(true);
    }

    /**
     * Create a new instance.
     *
     * @param strict {@code true} if looking up undefined environment variables should throw a
     *               {@link UndefinedEnvironmentVariableException}, {@code false} otherwise.
     * @throws UndefinedEnvironmentVariableException if the environment variable doesn't exist and strict behavior
     *                                               is enabled.
     */
    public EnvironmentVariableLookup(boolean strict) {
        this.strict = strict;
    }

    /**
     * {@inheritDoc}
     *
     * @throws UndefinedEnvironmentVariableException if the environment variable doesn't exist and strict behavior
     *                                               is enabled.
     */
    @Override
    public String lookup(String key) {
        final String value = System.getenv(key);

        if (value == null && strict) {
            throw new UndefinedEnvironmentVariableException("The environment variable '" + key
                    + "' is not defined; could not substitute the expression '${"
                    + key + "}'.");
        }

        return value;
    }
}
