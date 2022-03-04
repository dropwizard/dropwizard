package io.dropwizard.views.common;

import java.io.IOException;

/**
 * Signals that an error occurred during the rendering of a view.
 */
public class ViewRenderException extends IOException {
    private static final long serialVersionUID = -2972444466317717696L;

    /**
     * Constructs a {@link ViewRenderException} with the specified detail message.
     *
     * @param message The detail message (which is saved for later retrieval by the {@link
     *                #getMessage()} method)
     */
    public ViewRenderException(String message) {
        super(message);
    }

    /**
     * Constructs an {@code ViewRenderException} with the specified detail
     * message and cause.
     *
     * <p>
     * Note that the detail message associated with {@code cause} is <i>not</i>
     * automatically incorporated into this exception's detail message.
     *
     * @param message
     *            The detail message (which is saved for later retrieval by the
     *            {@link #getMessage()} method)
     *
     * @param cause
     *            The cause (which is saved for later retrieval by the
     *            {@link #getCause()} method). (A null value is permitted, and
     *            indicates that the cause is nonexistent or unknown.)
     *
     * @since 1.1.0
     */
    public ViewRenderException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Constructs an {@code ViewRenderException} with the specified cause and a
     * detail message of {@code (cause==null ? null : cause.toString())} (which
     * typically contains the class and detail message of {@code cause}). This
     * constructor is useful for IO exceptions that are little more than
     * wrappers for other throwables.
     *
     * @param cause
     *            The cause (which is saved for later retrieval by the
     *            {@link #getCause()} method). (A null value is permitted, and
     *            indicates that the cause is nonexistent or unknown.)
     *
     * @since 1.1.0
     */
    public ViewRenderException(Throwable cause) {
        super(cause);
    }

}
