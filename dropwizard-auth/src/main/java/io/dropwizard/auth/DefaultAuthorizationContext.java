package io.dropwizard.auth;

import java.security.Principal;

import javax.annotation.Nullable;
import javax.ws.rs.container.ContainerRequestContext;

public class DefaultAuthorizationContext<P extends Principal> extends AuthorizationContext<P> {
    DefaultAuthorizationContext(P principal, String role, @Nullable ContainerRequestContext requestContext) {
        super(principal, role, requestContext);
    }
}
