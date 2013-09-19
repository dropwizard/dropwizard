package io.dropwizard.views;

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
}
