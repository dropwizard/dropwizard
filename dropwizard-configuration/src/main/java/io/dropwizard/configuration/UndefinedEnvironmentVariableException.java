package io.dropwizard.configuration;

/**
 * An exception thrown, if a variable cannot be replaced by an {@link EnvironmentVariableSubstitutor} because no value is provided.
 */
public class UndefinedEnvironmentVariableException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    /**
     * Constructs a new exception with the given error message.
     *
     * @param errorMessage the error message
     */
    public UndefinedEnvironmentVariableException(String errorMessage) {
        super(errorMessage);
    }
}
