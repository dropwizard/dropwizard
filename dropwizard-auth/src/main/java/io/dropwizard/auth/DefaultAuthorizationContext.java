package io.dropwizard.auth;

import java.security.Principal;
import javax.ws.rs.container.ContainerRequestContext;
import org.checkerframework.checker.nullness.qual.Nullable;

/**
 * The default implementation of {@link AuthorizationContext},
 * which uses a {@link Principal}, a role and a {@link ContainerRequestContext} to
 * temporarily cache principals' role associations.
 *
 * @param <P> the type of principals
 */
public class DefaultAuthorizationContext<P extends Principal> extends AuthorizationContext<P> {
    DefaultAuthorizationContext(P principal, String role, @Nullable ContainerRequestContext requestContext) {
        super(principal, role, requestContext);
    }
}
