package io.dropwizard.auth;

import com.google.common.base.Preconditions;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestFilter;
import java.security.Principal;

@Priority(Priorities.AUTHENTICATION)
public abstract class AuthFilter<C, P extends Principal> implements ContainerRequestFilter {

    protected String prefix;
    protected String realm;
    protected Authenticator<C, P> authenticator;
    protected Authorizer<P> authorizer;
    protected UnauthorizedHandler unauthorizedHandler = new DefaultUnauthorizedHandler();

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
        private Authorizer<P> authorizer;

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
         * Builds an instance of the filter with provided an authenticator,
         * an authorizer, a prefix, and a realm.
         *
         * @return a new instance of a filter
         */
        public T buildAuthFilter() {
            Preconditions.checkArgument(realm != null, "Realm is not set");
            Preconditions.checkArgument(prefix != null, "Prefix is not set");
            Preconditions.checkArgument(authenticator != null, "Authenticator is not set");
            Preconditions.checkArgument(authorizer != null, "Authorizer is not set");

            T authFilter = newInstance();
            authFilter.authorizer = authorizer;
            authFilter.authenticator = authenticator;
            authFilter.prefix = prefix;
            authFilter.realm = realm;
            return authFilter;
        }

        protected abstract T newInstance();
    }
}
