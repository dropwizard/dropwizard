package io.dropwizard.auth;

import java.security.Principal;
import java.util.Optional;

/**
 * An interface for classes which authenticate user-provided credentials and return principal
 * objects.
 *
 * @param <C> the type of credentials the authenticator can authenticate
 * @param <P> the type of principals the authenticator returns
 */
public interface Authenticator<C, P extends Principal> {
    /**
     * Given a set of user-provided credentials, return an optional principal.
     *
     * If the credentials are valid and map to a principal, returns an {@link Optional#of(Object)}.
     *
     * If the credentials are invalid, returns an {@link Optional#empty()}.
     *
     * @param credentials a set of user-provided credentials
     * @return either an authenticated principal or an absent optional
     * @throws AuthenticationException if the credentials cannot be authenticated due to an
     *                                 underlying error
     */
    Optional<P> authenticate(C credentials) throws AuthenticationException;
}
