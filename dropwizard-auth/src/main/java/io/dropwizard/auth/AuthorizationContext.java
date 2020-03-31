package io.dropwizard.auth;

import java.security.Principal;
import java.util.Objects;

import javax.annotation.Nullable;
import javax.ws.rs.container.ContainerRequestContext;

class AuthorizationContext<P extends Principal> {
    private final P principal;
    private final String role;
    private final @Nullable
    ContainerRequestContext requestContext;

    AuthorizationContext(P principal, String role, @Nullable ContainerRequestContext requestContext) {
        this.principal = principal;
        this.role = role;
        this.requestContext = requestContext;
    }

    P getPrincipal() {
        return principal;
    }

    String getRole() {
        return role;
    }

    @Nullable ContainerRequestContext getRequestContext() {
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
