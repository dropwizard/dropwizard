package io.dropwizard.jersey.optional;

/**
 * An exception thrown when a resource endpoint attempts to write out an
 * optional that is empty.
 */
public class EmptyOptionalException extends RuntimeException {
    public static final EmptyOptionalException INSTANCE = new EmptyOptionalException();

    private EmptyOptionalException() { }
}
