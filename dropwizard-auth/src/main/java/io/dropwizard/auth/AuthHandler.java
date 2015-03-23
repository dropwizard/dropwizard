package io.dropwizard.auth;

import com.google.common.base.Function;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.core.SecurityContext;
import java.security.Principal;

public abstract class AuthHandler<C, P extends Principal> {
    protected String prefix;
    protected String realm;
    protected Authenticator<C, P> authenticator;
    protected UnauthorizedHandler unauthorizedHandler;
    protected Function<Tuple, SecurityContext> securityContextFunction;

    protected void setUnauthorizedHandler(UnauthorizedHandler unauthorizedHandler) {
        this.unauthorizedHandler = unauthorizedHandler;
    }

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

    protected static class Tuple {
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

    public abstract void handle(ContainerRequestContext containerRequestContext, boolean authRequired);

    public Function<Tuple, SecurityContext> getSecurityContextFunction() {
        return securityContextFunction;
    }

    public static abstract class AuthHandlerBuilder<C, P extends Principal, T extends AuthHandler<C, P>, A extends Authenticator<C, P>> {
        protected String realm = "realm";
        protected String prefix = "Basic";
        protected Authenticator<C, P> authenticator;
        protected UnauthorizedHandler unauthorizedHandler = new DefaultUnauthorizedHandler();
        protected Function<Tuple, SecurityContext> securityContextFunction =  new Function<Tuple, SecurityContext>() {
            @Override
            public SecurityContext apply(final Tuple input) {
                return new SecurityContext() {
                    @Override
                    public Principal getUserPrincipal() {
                        return input.getPrincipal();
                    }

                    @Override
                    public boolean isUserInRole(String role) {
                        return false;
                    }

                    @Override
                    public boolean isSecure() {
                        return input.getContainerRequestContext().getSecurityContext().isSecure();
                    }

                    @Override
                    public String getAuthenticationScheme() {
                        return SecurityContext.BASIC_AUTH;
                    }
                };
            }
        };

        public AuthHandlerBuilder setRealm(String realm) {
            this.realm = realm;
            return this;
        }

        public AuthHandlerBuilder setPrefix(String prefix) {
            this.prefix = prefix;
            return this;
        }

        public AuthHandlerBuilder setUnauthorizedHandler(UnauthorizedHandler unauthorizedHandler) {
            this.unauthorizedHandler = unauthorizedHandler;
            return this;
        }

        public AuthHandlerBuilder setSecurityContextFunction(Function<Tuple, SecurityContext> securityContextFunction) {
            this.securityContextFunction = securityContextFunction;
            return this;
        }

        public AuthHandlerBuilder setAuthenticator(A authenticator) {
            this.authenticator = authenticator;
            return this;
        }

        public abstract T buildAuthHandler();
    }
}
