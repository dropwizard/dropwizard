package io.dropwizard.jersey.optional;

/**
 * An exception thrown when a resource endpoint attempts to write out an
 * optional that is empty.
 */
public class EmptyOptionalException extends RuntimeException {
    /**
     * Auto-generated by Eclipse.
     */
    private static final long serialVersionUID = -3398853218754085781L;

    public static final EmptyOptionalException INSTANCE = new EmptyOptionalException();

    private EmptyOptionalException() {}
}
