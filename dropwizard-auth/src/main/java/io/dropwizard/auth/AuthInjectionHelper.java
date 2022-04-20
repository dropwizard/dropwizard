package io.dropwizard.auth;

import org.glassfish.jersey.internal.inject.InjectionManager;

import javax.ws.rs.container.ContainerRequestFilter;

/**
 * Utility class to help with injection into auth filters.
 */
class AuthInjectionHelper {
    private AuthInjectionHelper() {
        // utility class
    }

    static void inject(InjectionManager injectionManager, ContainerRequestFilter filter) {
        injectionManager.inject(filter);

        // if instance is AuthFilter, inject Authenticator and Authorizer instances as well
        if (filter instanceof AuthFilter) {
            AuthFilter<?, ?> authFilter = (AuthFilter<?, ?>) filter;
            if (authFilter.authenticator != null) {
                injectionManager.inject(authFilter.authenticator);
            }
            if (authFilter.authorizer != null) {
                injectionManager.inject(authFilter.authorizer);
            }
        }
    }
}
