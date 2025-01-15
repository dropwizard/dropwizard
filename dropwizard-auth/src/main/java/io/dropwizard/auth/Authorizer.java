package io.dropwizard.auth;

import jakarta.ws.rs.container.ContainerRequestContext;
import org.jspecify.annotations.Nullable;

import java.security.Principal;

/**
 * An interface for classes which authorize principal objects.
 *
 * @param <P> the type of principals
 */
public interface Authorizer<P extends Principal> {
    /**
     * Decides if access is granted for the given principal in the given role.
     *
     * @param principal      a {@link Principal} object, representing a user
     * @param role           a user role
     * @param requestContext a request context.
     * @return {@code true}, if the access is granted, {@code false otherwise}
     * @since 2.0
     */
    boolean authorize(P principal, String role, @Nullable ContainerRequestContext requestContext);

    /**
     * Returns an {@link AuthorizationContext} object, to be used in {@link CachingAuthorizer} as cache key.
     * @param principal      a {@link Principal} object, representing a user
     * @param role           a user role
     * @param requestContext a request context.
     * @return {@link AuthorizationContext} object, to be used in {@link CachingAuthorizer}.
     * @since 2.1
     */
    default AuthorizationContext<P> getAuthorizationContext(P principal, String role, @Nullable ContainerRequestContext requestContext) {
        return new DefaultAuthorizationContext<>(principal, role, requestContext);
    }
}
