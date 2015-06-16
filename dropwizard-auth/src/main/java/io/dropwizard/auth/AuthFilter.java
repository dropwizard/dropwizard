package io.dropwizard.auth;

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

    protected void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    protected void setRealm(String realm) {
        this.realm = realm;
    }

    protected void setAuthenticator(Authenticator<C, P> authenticator) {
        this.authenticator = authenticator;
    }

    protected void setAuthorizer(Authorizer<P> authorizer) {
        this.authorizer = authorizer;
    }

    public abstract static class AuthFilterBuilder<C, P extends Principal, T extends AuthFilter<C, P>, A extends Authenticator<C, P>> {
        protected String realm = "realm";
        protected String prefix = "Basic";
        protected Authenticator<C, P> authenticator;
        protected Authorizer<P> authorizer;

        public AuthFilterBuilder<C, P, T, A>  setRealm(String realm) {
            this.realm = realm;
            return this;
        }

        public AuthFilterBuilder<C, P, T, A>  setPrefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public AuthFilterBuilder<C, P, T, A>  setAuthorizer(Authorizer<P> authorizer) {
            this.authorizer = authorizer;
            return this;
        }

        public AuthFilterBuilder<C, P, T, A> setAuthenticator(A authenticator) {
            this.authenticator = authenticator;
            return this;
        }

        public abstract T buildAuthFilter();
    }
}
