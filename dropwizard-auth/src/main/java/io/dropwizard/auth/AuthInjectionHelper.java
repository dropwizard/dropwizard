package io.dropwizard.auth;

import jakarta.ws.rs.container.ContainerRequestFilter;
import org.glassfish.jersey.internal.inject.InjectionManager;

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
        if (filter instanceof AuthFilter<?, ?> authFilter) {
            if (authFilter.authenticator != null) {
                injectionManager.inject(authFilter.authenticator);
            }
            if (authFilter.authorizer != null) {
                injectionManager.inject(authFilter.authorizer);
            }
        }
    }
}
