package io.dropwizard.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.SecurityContext;
import org.jspecify.annotations.Nullable;
import org.eclipse.jetty.ee10.servlet.ServletApiRequest;
import org.eclipse.jetty.security.AuthenticationState;
import org.eclipse.jetty.security.UserIdentity;
import org.eclipse.jetty.security.authentication.LoginAuthenticator;
import org.eclipse.jetty.server.Request;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.util.collection.Ref;

import javax.security.auth.Subject;
import java.lang.reflect.Type;
import java.security.Principal;
import java.util.Set;

class JettyAuthenticationUtil {
    private static final Type HTTP_SERVLET_REQUEST_REF_TYPE = (new GenericType<Ref<HttpServletRequest>>() {}).getType();

    static void setJettyAuthenticationIfPossible(SecurityContext securityContext, @Nullable InjectionManager injectionManager) {
        if (injectionManager == null) {
            return;
        }
        final Ref<HttpServletRequest> requestRef = injectionManager.getInstance(HTTP_SERVLET_REQUEST_REF_TYPE);
        if (requestRef == null) {
            return;
        }
        HttpServletRequest request = requestRef.get();
        if (!(request instanceof ServletApiRequest servletApiRequest)) {
            return;
        }

        AuthenticationState.Succeeded authentication = new LoginAuthenticator.UserAuthenticationSucceeded(
            securityContext.getAuthenticationScheme(), new DropwizardJettyUserIdentity(securityContext));
        Request.setAuthenticationState(servletApiRequest.getRequest(), authentication);
    }

    private static class DropwizardJettyUserIdentity implements UserIdentity {
        private final Subject subject;
        private final SecurityContext securityContext;

        public DropwizardJettyUserIdentity(SecurityContext securityContext) {
            this.securityContext = securityContext;
            this.subject = new Subject(true, Set.of(securityContext.getUserPrincipal()), Set.of(), Set.of());
        }

        @Override
        public Subject getSubject() {
            return subject;
        }

        @Override
        public Principal getUserPrincipal() {
            return securityContext.getUserPrincipal();
        }

        @Override
        public boolean isUserInRole(String role) {
            return securityContext.isUserInRole(role);
        }
    }
}
