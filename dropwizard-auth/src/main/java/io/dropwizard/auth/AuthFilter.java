package io.dropwizard.auth;

import com.google.common.base.Preconditions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.ws.rs.InternalServerErrorException;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.SecurityContext;
import java.security.Principal;
import java.util.Optional;

@Priority(Priorities.AUTHENTICATION)
public abstract class AuthFilter<C, P extends Principal> implements ContainerRequestFilter {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected String prefix;
    protected String realm;
    protected Authenticator<C, P> authenticator;
    protected Authorizer<P> authorizer;
    protected UnauthorizedHandler unauthorizedHandler;

    /**
     * Abstract builder for auth filters.
     *
     * @param <C> the type of credentials that the filter accepts
     * @param <P> the type of the principal that the filter accepts
     */
    public abstract static class AuthFilterBuilder<C, P extends Principal, T extends AuthFilter<C, P>> {

        private String realm = "realm";
        private String prefix = "Basic";
        private Authenticator<C, P> authenticator;
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
            Preconditions.checkArgument(realm != null, "Realm is not set");
            Preconditions.checkArgument(prefix != null, "Prefix is not set");
            Preconditions.checkArgument(authenticator != null, "Authenticator is not set");
            Preconditions.checkArgument(authorizer != null, "Authorizer is not set");
            Preconditions.checkArgument(unauthorizedHandler != null, "Unauthorized handler is not set");

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
     * Authenticates a request with user credentials and setup the security context.
     *
     * @param requestContext the context of the request
     * @param credentials    the user credentials
     * @param scheme         the authentication scheme; one of {@code BASIC_AUTH, FORM_AUTH, CLIENT_CERT_AUTH, DIGEST_AUTH}.
     *                       See {@link SecurityContext}
     * @return {@code true}, if the request is authenticated, otherwise {@code false}
     */
    protected boolean authenticate(ContainerRequestContext requestContext, C credentials, String scheme) {
        try {
            if (credentials == null) {
                return false;
            }

            final Optional<P> principal = authenticator.authenticate(credentials);
            if (!principal.isPresent()) {
                return false;
            }

            final SecurityContext securityContext = requestContext.getSecurityContext();
            final boolean secure = securityContext != null && securityContext.isSecure();
            
            requestContext.setSecurityContext(new SecurityContext() {
                @Override
                public Principal getUserPrincipal() {
                    return principal.get();
                }

                @Override
                public boolean isUserInRole(String role) {
                    return authorizer.authorize(principal.get(), role);
                }

                @Override
                public boolean isSecure() {
                    return secure;
                }

                @Override
                public String getAuthenticationScheme() {
                    return scheme;
                }
            });
            return true;
        } catch (AuthenticationException e) {
            logger.warn("Error authenticating credentials", e);
            throw new InternalServerErrorException();
        }
    }
}
