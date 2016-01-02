package io.dropwizard.auth;

import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.internal.inject.AbstractContainerRequestValueFactory;

import java.security.Principal;

/**
 * A value factory which extracts the {@link Principal} from the
 * current {@link ContainerRequest} instance.
 */
class PrincipalContainerRequestValueFactory extends AbstractContainerRequestValueFactory<Principal> {
    /**
     * @return {@link Principal} stored on the request, or {@code null}
     *         if no object was found.
     */
    public Principal provide() {
        final Principal principal = getContainerRequest().getSecurityContext().getUserPrincipal();
        if (principal == null) {
            throw new IllegalStateException("Cannot inject a custom principal into unauthenticated request");
        }
        return principal;
    }
}
