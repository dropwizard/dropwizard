package io.dropwizard.auth;

import javax.annotation.Nullable;
import javax.ws.rs.container.ContainerRequestContext;
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
     * @param principal a {@link Principal} object, representing a user
     * @param role      a user role
     * @return {@code true}, if the access is granted, {@code false otherwise}
     * @deprecated Use {@link #authorize(Principal, String, ContainerRequestContext)} instead
     */
    @Deprecated
    boolean authorize(P principal, String role);

    /**
     * Decides if access is granted for the given principal in the given role.
     *
     * @param principal      a {@link Principal} object, representing a user
     * @param role           a user role
     * @param requestContext a request context.
     * @return {@code true}, if the access is granted, {@code false otherwise}
     * @since 2.0
     */
    @SuppressWarnings("deprecation")
    default boolean authorize(P principal, String role, @Nullable ContainerRequestContext requestContext) {
        return authorize(principal, role);
    }

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
