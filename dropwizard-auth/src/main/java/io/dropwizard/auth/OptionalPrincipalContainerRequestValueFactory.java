package io.dropwizard.auth;

import org.glassfish.jersey.server.ContainerRequest;

import java.security.Principal;
import java.util.Optional;

/**
 * A value factory which extracts an {@link Optional optional} {@link
 * Principal} from the current {@link ContainerRequest} instance.
 */
class OptionalPrincipalContainerRequestValueFactory {
    private final ContainerRequest request;

    public OptionalPrincipalContainerRequestValueFactory(ContainerRequest request) {
        this.request = request;
    }

    /**
     * @return {@link Optional}{@code <}{@link Principal}{@code >}
     *         stored on the request, or {@code Optional.empty()} if
     *         no object was found.
     */
    public Optional<Principal> provide() {
        return Optional.ofNullable(request.getSecurityContext().getUserPrincipal());
    }
}
