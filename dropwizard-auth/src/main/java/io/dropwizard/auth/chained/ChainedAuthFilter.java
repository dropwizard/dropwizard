package io.dropwizard.auth.chained;

import io.dropwizard.auth.AuthFilter;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.security.Principal;
import java.util.List;

@Priority(Priorities.AUTHENTICATION)
public class ChainedAuthFilter<C, P extends Principal> extends AuthFilter<C, P> {
    private final List<AuthFilter<C, P>> handlers;

    public ChainedAuthFilter(List<AuthFilter<C, P>> handlers) {
        this.handlers = handlers;
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        WebApplicationException firstException = null;
        for (AuthFilter<C, P> authFilter : handlers) {
            SecurityContext securityContext = containerRequestContext.getSecurityContext();
            try {
                authFilter.filter(containerRequestContext);
                if (securityContext != containerRequestContext.getSecurityContext()) {
                    return;
                }
            } catch (WebApplicationException e) {
                if(firstException == null) {
                    firstException = e;
                }
            }
        }

        throw firstException;
    }
}
