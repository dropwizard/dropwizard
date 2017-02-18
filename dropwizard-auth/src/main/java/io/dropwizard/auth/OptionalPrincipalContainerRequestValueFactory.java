package io.dropwizard.auth;

import org.glassfish.jersey.server.ContainerRequest;
import org.glassfish.jersey.server.internal.inject.AbstractContainerRequestValueFactory;

import java.util.Optional;
import java.security.Principal;

/**
 * A value factory which extracts an {@link Optional optional} {@link
 * Principal} from the current {@link ContainerRequest} instance.
 */
class OptionalPrincipalContainerRequestValueFactory
    extends AbstractContainerRequestValueFactory<Optional<Principal>> {
    /**
     * @return {@link Optional}{@code <}{@link Principal}{@code >}
     *         stored on the request, or {@code Optional.empty()} if
     *         no object was found.
     */
    @Override
    public Optional<Principal> provide() {
        return Optional.ofNullable(getContainerRequest().getSecurityContext().getUserPrincipal());
    }
}
