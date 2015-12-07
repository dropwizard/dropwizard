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

/**
 * Chains together authFilters, short circuits when the first filter
 * successfully authenticates
 *
 * N.B. AuthFilters can be chained together as long as they produce the same type
 * of Principal. This is not enforced by the type system at compile time, using
 * inconsistent principals will lead to runtime errors
 *
 * There is no requirement for the filters that are chained to use the same type for credentials.
 * The reason is that the ChainedFilter delegates to a filter which encapsulates
 * the authenticator and credential type
 *
 *
 * @param <C> the type of Credentials to be authenticated
 * @param <P> the type of the Principal
 */
@Priority(Priorities.AUTHENTICATION)
public class ChainedAuthFilter<C, P extends Principal> extends AuthFilter<C, P> {
    private final List<AuthFilter> handlers;

    public ChainedAuthFilter(List<AuthFilter> handlers) {
        this.handlers = handlers;
    }

    @Override
    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        WebApplicationException firstException = null;
        for (AuthFilter authFilter : handlers) {
            final SecurityContext securityContext = containerRequestContext.getSecurityContext();
            try {
                authFilter.filter(containerRequestContext);
                if (securityContext != containerRequestContext.getSecurityContext()) {
                    return;
                }
            } catch (WebApplicationException e) {
                if (firstException == null) {
                    firstException = e;
                }
            }
        }

        throw firstException;
    }
}
