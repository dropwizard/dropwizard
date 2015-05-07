package io.dropwizard.auth;

import com.google.common.base.Function;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.SecurityContext;
import java.security.Principal;

@Priority(Priorities.AUTHENTICATION)
public abstract class AuthFilter<C, P extends Principal> implements ContainerRequestFilter{
    protected String prefix;
    protected String realm;
    protected Authenticator<C, P> authenticator;
    protected Function<Tuple, SecurityContext> securityContextFunction;
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

    protected void setSecurityContextFunction(Function<Tuple, SecurityContext> securityContextFunction) {
        this.securityContextFunction = securityContextFunction;
    }

    protected Function<Tuple, SecurityContext> getSecurityContextFunction() {
        return securityContextFunction;
    }

    public static class Tuple {
        private ContainerRequestContext containerRequestContext;
        private Principal principal;

        public Tuple(ContainerRequestContext containerRequestContext, Principal principal) {
            this.containerRequestContext = containerRequestContext;
            this.principal = principal;
        }

        public ContainerRequestContext getContainerRequestContext() {
            return containerRequestContext;
        }

        public Principal getPrincipal() {
            return principal;
        }
    }

    public static abstract class AuthFilterBuilder<C, P extends Principal, T extends AuthFilter<C, P>, A extends Authenticator<C, P>> {
        protected String realm = "realm";
        protected String prefix = "Basic";
        protected Authenticator<C, P> authenticator;
        protected Function<Tuple, SecurityContext> securityContextFunction;

        public AuthFilterBuilder setRealm(String realm) {
            this.realm = realm;
            return this;
        }

        public AuthFilterBuilder setPrefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public AuthFilterBuilder setSecurityContextFunction(Function<Tuple, SecurityContext> securityContextFunction) {
            this.securityContextFunction = securityContextFunction;
            return this;
        }

        public AuthFilterBuilder setAuthenticator(A authenticator) {
            this.authenticator = authenticator;
            return this;
        }

        public abstract T buildAuthFilter();
    }
}
