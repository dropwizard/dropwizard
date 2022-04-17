package io.dropwizard.auth;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.glassfish.jersey.internal.inject.InjectionManager;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import java.io.IOException;

/**
 * A {@link ContainerRequestFilter} decorator providing dependency injection.
 * Thereby the filter must be of type {@link AuthFilter} or {@link WebApplicationExceptionCatchingFilter},
 * from which an {@link AuthFilter} can be obtained.
 *
 * If {@link Authenticator} and {@link Authorizer} instances of this {@link AuthFilter} are not {@code null}, dependency
 * injection is provided for these too.
 */
@Priority(Priorities.AUTHENTICATION)
class InjectingFilter implements ContainerRequestFilter {
    @Nullable
    private final InjectionManager injectionManager;
    private final AuthFilter<?, ?> filter;
    private final ContainerRequestFilter rawAuthFilter;

    public InjectingFilter(@Nullable InjectionManager injectionManager, ContainerRequestFilter filter) {
        this.injectionManager = injectionManager;
        ContainerRequestFilter tempAuthFilter = filter;
        if (filter instanceof WebApplicationExceptionCatchingFilter) {
            tempAuthFilter = ((WebApplicationExceptionCatchingFilter) filter).getUnderlying();
        }
        if (!(tempAuthFilter instanceof AuthFilter)) {
            throw new IllegalArgumentException("The filter must be an instance of AuthFilter");
        }
        this.filter = (AuthFilter<?, ?>) tempAuthFilter;
        this.rawAuthFilter = filter;
    }

    @Override
    public void filter(ContainerRequestContext requestContext) throws IOException {
        if (injectionManager != null) {
            injectionManager.inject(filter);
            if (filter.authenticator != null) {
                injectionManager.inject(filter.authenticator);
            }
            if (filter.authorizer != null) {
                injectionManager.inject(filter.authorizer);
            }
        }
        rawAuthFilter.filter(requestContext);
    }
}
