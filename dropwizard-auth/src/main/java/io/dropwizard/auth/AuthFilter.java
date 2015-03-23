package io.dropwizard.auth;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import java.io.IOException;

@Priority(Priorities.AUTHENTICATION)
public class AuthFilter implements ContainerRequestFilter {
    private AuthHandler authHandler;
    private boolean authRequired;


    public AuthFilter(AuthHandler authHandler, boolean authRequired) {
        this.authHandler = authHandler;
        this.authRequired = authRequired;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        authHandler.handle(requestContext, authRequired);
    }
}