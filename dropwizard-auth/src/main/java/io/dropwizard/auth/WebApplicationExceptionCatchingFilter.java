package io.dropwizard.auth;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import java.io.IOException;

import static java.util.Objects.requireNonNull;

/**
 * A {@link ContainerRequestFilter} decorator which catches any {@link
 * WebApplicationException WebApplicationExceptions} thrown by an
 * underlying {@code ContextRequestFilter}.
 */
@Priority(Priorities.AUTHENTICATION)
class WebApplicationExceptionCatchingFilter implements ContainerRequestFilter {
    private final ContainerRequestFilter underlying;

    public WebApplicationExceptionCatchingFilter(ContainerRequestFilter underlying) {
        requireNonNull(underlying, "Underlying ContainerRequestFilter is not set");
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
