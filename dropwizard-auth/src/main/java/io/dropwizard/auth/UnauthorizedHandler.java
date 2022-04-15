package io.dropwizard.auth;

import org.jetbrains.annotations.Nullable;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Response;

public interface UnauthorizedHandler {
    @Nullable
    default Response buildResponse(String prefix, String realm) {
        return null;
    }

    /**
     * This method allows overriding the exception thrown from an {@link AuthFilter} instance.
     * This provides the possibility to catch auth exceptions in a custom {@link javax.ws.rs.ext.ExceptionMapper}.
     * To process the exception in a custom {@link javax.ws.rs.ext.ExceptionMapper}, the response entity MUST be empty
     * when using a subclass of {@link WebApplicationException}.
     * Else the {@link javax.ws.rs.ext.ExceptionMapper} won't get invoked.
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
