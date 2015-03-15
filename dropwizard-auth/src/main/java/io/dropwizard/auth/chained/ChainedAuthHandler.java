package io.dropwizard.auth.chained;

import io.dropwizard.auth.AuthHandler;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import java.security.Principal;
import java.util.List;

public class ChainedAuthHandler<C, P extends Principal> extends AuthHandler<C, P> {
    private final List<AuthHandler<C, P>> handlers;
    public ChainedAuthHandler(List<AuthHandler<C, P>> handlers) {
        this.handlers = handlers;
    }

    @Override
    public void handle(ContainerRequestContext containerRequestContext, boolean authRequired) {
        WebApplicationException firstException = null;
        for(AuthHandler<C, P> authHandler: handlers) {
            try {
                authHandler.handle(containerRequestContext, authRequired);
                return;
            }
            catch (WebApplicationException e) {
                if(firstException == null) {
                    firstException = e;
                }
            }
        }
        throw firstException;
    }
}
