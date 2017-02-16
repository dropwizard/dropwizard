package io.dropwizard.auth;

import java.io.IOException;
import java.security.Principal;
import java.util.Optional;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;

/**
 * A {@link ContainerRequestFilter} decorator which catches any {@link
 * WebApplicationException WebApplicationExceptions} thrown by an
 * underlying {@code ContextRequestFilter}.
 */
class WebApplicationExceptionCatchingFilter implements ContainerRequestFilter {
    private final ContainerRequestFilter underlying;

    public WebApplicationExceptionCatchingFilter(ContainerRequestFilter underlying) {
        this.underlying = underlying;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        try {
            underlying.filter(requestContext);
        } catch (WebApplicationException err) {
            // Pass through.
        }
    }
}
