package io.dropwizard.auth;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.core.Response;
import org.jspecify.annotations.Nullable;

public interface UnauthorizedHandler {
    @Nullable
    default Response buildResponse(String prefix, String realm) {
        return null;
    }

    /**
     * This method allows overriding the exception thrown from an {@link AuthFilter} instance.
     * This provides the possibility to catch auth exceptions in a custom {@link jakarta.ws.rs.ext.ExceptionMapper}.
     * To process the exception in a custom {@link jakarta.ws.rs.ext.ExceptionMapper}, the response entity MUST be empty
     * when using a subclass of {@link WebApplicationException}.
     * Else the {@link jakarta.ws.rs.ext.ExceptionMapper} won't get invoked.
     *
     * The default implementation of this method creates a {@link WebApplicationException} containing the response built
     * in {@link #buildResponse(String, String)}.
     *
     * @since 2.1.0
     */
    default RuntimeException buildException(String prefix, String realm) {
        return new WebApplicationException(buildResponse(prefix, realm));
    }
}
