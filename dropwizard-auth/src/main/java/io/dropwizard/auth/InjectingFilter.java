package io.dropwizard.auth;

import org.glassfish.jersey.internal.inject.InjectionManager;

import javax.annotation.Nullable;
import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import java.io.IOException;

/**
 * A {@link ContainerRequestFilter} decorator providing dependency injection.
 *
 * If an {@link AuthFilter} instance can be obtained from the {@link ContainerRequestFilter} and the
 * {@link Authenticator} and {@link Authorizer} instances of this {@link AuthFilter} are not {@code null}, dependency
 * injection is provided for these too.
 */
@Priority(Priorities.AUTHENTICATION)
class InjectingFilter implements ContainerRequestFilter {
    @Nullable
    private final InjectionManager injectionManager;
    private final ContainerRequestFilter rawAuthFilter;

    public InjectingFilter(@Nullable InjectionManager injectionManager, ContainerRequestFilter filter) {
        this.injectionManager = injectionManager;
        this.rawAuthFilter = filter;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (injectionManager != null) {
            ContainerRequestFilter tempAuthFilter = rawAuthFilter;
            if (tempAuthFilter instanceof WebApplicationExceptionCatchingFilter) {
                tempAuthFilter = ((WebApplicationExceptionCatchingFilter) tempAuthFilter).getUnderlying();
            }
            injectionManager.inject(tempAuthFilter);
            if (tempAuthFilter instanceof AuthFilter<?, ?>) {
                AuthFilter<?, ?> filter = (AuthFilter<?, ?>) tempAuthFilter;
                if (filter.authenticator != null) {
                    injectionManager.inject(filter.authenticator);
                }
                if (filter.authorizer != null) {
                    injectionManager.inject(filter.authorizer);
                }
            }
        }
        rawAuthFilter.filter(requestContext);
    }
}
