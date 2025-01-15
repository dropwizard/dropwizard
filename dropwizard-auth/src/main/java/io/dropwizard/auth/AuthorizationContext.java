package io.dropwizard.auth;

import jakarta.ws.rs.container.ContainerRequestContext;
import org.jspecify.annotations.Nullable;

import java.security.Principal;
import java.util.Objects;

public abstract class AuthorizationContext<P extends Principal> {
    private final P principal;
    private final String role;
    private final @Nullable
    ContainerRequestContext requestContext;

    public AuthorizationContext(P principal, String role, @Nullable ContainerRequestContext requestContext) {
        this.principal = principal;
        this.role = role;
        this.requestContext = requestContext;
    }

    public P getPrincipal() {
        return principal;
    }

    public String getRole() {
        return role;
    }

    @Nullable public ContainerRequestContext getRequestContext() {
        return requestContext;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthorizationContext<?> that = (AuthorizationContext<?>) o;
        return Objects.equals(principal, that.principal) &&
            Objects.equals(role, that.role) &&
            Objects.equals(requestContext, that.requestContext);
    }

    @Override
    public int hashCode() {
        return Objects.hash(principal, role, requestContext);
    }
}
