package io.dropwizard.auth;

import javax.annotation.Nullable;
import javax.ws.rs.core.UriInfo;
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
    boolean authorize(P principal, String role);

    /**
     * Decides if access is granted for the given principal in the given role.
     *
     * @param principal a {@link Principal} object, representing a user
     * @param role a user role
     * @param uriInfo a request URI information
     * @return {@code true}, if the access is granted, {@code false otherwise}
     */
    default boolean authorize(P principal, String role, @Nullable UriInfo uriInfo) {
        return authorize(principal, role);
    }
}
