package io.dropwizard.auth;

import jakarta.annotation.Priority;
import jakarta.inject.Inject;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.SecurityContext;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import java.util.Optional;

import static java.util.Objects.requireNonNull;

@Priority(Priorities.AUTHENTICATION)
public abstract class AuthFilter<C, P extends Principal> implements ContainerRequestFilter {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected String prefix =  "Basic";
    protected String realm = "realm";
    protected Authenticator<C, P> authenticator = credentials -> Optional.empty();
    protected Authorizer<P> authorizer = new PermitAllAuthorizer<>();
    protected UnauthorizedHandler unauthorizedHandler = new DefaultUnauthorizedHandler();

    @Nullable
    @Inject
    private InjectionManager injectionManager;

    /**
     * Abstract builder for auth filters.
     *
     * @param <C> the type of credentials that the filter accepts
     * @param <P> the type of the principal that the filter accepts
     */
    public abstract static class AuthFilterBuilder<C, P extends Principal, T extends AuthFilter<C, P>> {

        private String realm = "realm";
        private String prefix = "Basic";
        private Authenticator<C, P> authenticator = credentials -> Optional.empty();
        private Authorizer<P> authorizer = new PermitAllAuthorizer<>();
        private UnauthorizedHandler unauthorizedHandler = new DefaultUnauthorizedHandler();

        /**
         * Sets the given realm
         *
         * @param realm a realm
         * @return the current builder
         */
        public AuthFilterBuilder<C, P, T> setRealm(String realm) {
            this.realm = realm;
            return this;
        }

        /**
         * Sets the given prefix
         *
         * @param prefix a prefix
         * @return the current builder
         */
        public AuthFilterBuilder<C, P, T> setPrefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        /**
         * Sets the given authorizer
         *
         * @param authorizer an {@link Authorizer}
         * @return the current builder
         */
        public AuthFilterBuilder<C, P, T> setAuthorizer(Authorizer<P> authorizer) {
            this.authorizer = authorizer;
            return this;
        }

        /**
         * Sets the given authenticator
         *
         * @param authenticator an {@link Authenticator}
         * @return the current builder
         */
        public AuthFilterBuilder<C, P, T> setAuthenticator(Authenticator<C, P> authenticator) {
            this.authenticator = authenticator;
            return this;
        }

        /**
         * Sets the given unauthorized handler
         *
         * @param unauthorizedHandler an {@link UnauthorizedHandler}
         * @return the current builder
         */
        public AuthFilterBuilder<C, P, T> setUnauthorizedHandler(UnauthorizedHandler unauthorizedHandler) {
            this.unauthorizedHandler = unauthorizedHandler;
            return this;
        }

        /**
         * Builds an instance of the filter with a provided authenticator,
         * an authorizer, a prefix, and a realm.
         *
         * @return a new instance of the filter
         */
        public T buildAuthFilter() {
            requireNonNull(realm, "Realm is not set");
            requireNonNull(prefix, "Prefix is not set");
            requireNonNull(authenticator, "Authenticator is not set");
            requireNonNull(authorizer, "Authorizer is not set");
            requireNonNull(unauthorizedHandler, "Unauthorized handler is not set");

            final T authFilter = newInstance();
            authFilter.authorizer = authorizer;
            authFilter.authenticator = authenticator;
            authFilter.prefix = prefix;
            authFilter.realm = realm;
            authFilter.unauthorizedHandler = unauthorizedHandler;
            return authFilter;
        }

        protected abstract T newInstance();
    }

    /**
     * Authenticates a request with user credentials and set up the security context.
     *
     * @param requestContext the context of the request
     * @param credentials    the user credentials
     * @param scheme         the authentication scheme; one of {@code BASIC_AUTH, FORM_AUTH, CLIENT_CERT_AUTH, DIGEST_AUTH}.
     *                       See {@link SecurityContext}
     * @return {@code true}, if the request is authenticated, otherwise {@code false}
     */
    protected boolean authenticate(ContainerRequestContext requestContext, @Nullable C credentials, String scheme) {
        try {
            if (credentials == null) {
                return false;
            }

            final Optional<P> principal = authenticator.authenticate(credentials);
            if (principal.isEmpty()) {
                return false;
            }

            final P prince = principal.get();
            final SecurityContext securityContext = requestContext.getSecurityContext();
            final boolean secure = securityContext != null && securityContext.isSecure();

            SecurityContext dropwizardAuthenticatedSecurityContext = new SecurityContext() {
                @Override
                public Principal getUserPrincipal() {
                    return prince;
                }

                @Override
                public boolean isUserInRole(String role) {
                    return authorizer.authorize(prince, role, requestContext);
                }

                @Override
                public boolean isSecure() {
                    return secure;
                }

                @Override
                public String getAuthenticationScheme() {
                    return scheme;
                }
            };
            requestContext.setSecurityContext(dropwizardAuthenticatedSecurityContext);
            JettyAuthenticationUtil.setJettyAuthenticationIfPossible(dropwizardAuthenticatedSecurityContext, injectionManager);
            return true;
        } catch (AuthenticationException e) {
            logger.warn("Error authenticating credentials", e);
            throw new InternalServerErrorException();
        }
    }
}
