package io.dropwizard.auth;

import jakarta.ws.rs.container.ContainerRequestContext;
import org.jspecify.annotations.Nullable;

import java.security.Principal;

/**
 * An {@link Authorizer} that grants access for any principal in any role.
 *
 * @param <P> the type of the principal
 */
public class PermitAllAuthorizer<P extends Principal> implements Authorizer<P> {

    @Override
    public boolean authorize(P principal, String role, @Nullable ContainerRequestContext ctx) {
        return true;
    }
}
