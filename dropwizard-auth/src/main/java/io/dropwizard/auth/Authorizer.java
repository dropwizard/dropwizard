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
     * @param role a user role
     * @return {@code true}, if the access is granted, {@code false otherwise}
     */
    @Deprecated
    boolean authorize(P principal, String role);

    /**
     * Decides if access is granted for the given principal in the given role.
     *
     * @param principal a {@link Principal} object, representing a user
     * @param role a user role
     * @param requestContext a request context.
     * @return {@code true}, if the access is granted, {@code false otherwise}
     */
    @SuppressWarnings("deprecation")
    default boolean authorize(P principal, String role, @Nullable ContainerRequestContext requestContext) {
        return authorize(principal, role);
    }
}
