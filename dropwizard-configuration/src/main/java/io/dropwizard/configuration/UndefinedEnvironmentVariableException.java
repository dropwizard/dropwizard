package io.dropwizard.configuration;

public class UndefinedEnvironmentVariableException extends RuntimeException {
    public UndefinedEnvironmentVariableException(String errorMessage) {
        super(errorMessage);
    }
}
