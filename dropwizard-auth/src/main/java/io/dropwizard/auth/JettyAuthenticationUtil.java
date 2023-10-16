package io.dropwizard.auth;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.core.GenericType;
import jakarta.ws.rs.core.SecurityContext;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.eclipse.jetty.security.AbstractUserAuthentication;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.UserIdentity;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.internal.util.collection.Ref;

import javax.security.auth.Subject;
import java.lang.reflect.Type;
import java.security.Principal;
import java.util.Optional;
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
        if (!(request instanceof Request)) {
            return;
        }
        Request jettyRequest = (Request) request;

        Authentication authentication = new DropwizardJettyAuthentication(securityContext);
        jettyRequest.setAuthentication(authentication);
    }

    private static class DropwizardJettyAuthentication extends AbstractUserAuthentication {
        public DropwizardJettyAuthentication(SecurityContext securityContext) {
            super(securityContext.getAuthenticationScheme(), new DropwizardJettyUserIdentity(securityContext));
        }
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
        public boolean isUserInRole(String role, Scope scope) {
            // Servlet spec forbids the role name "*", so return false in that case
            if ("*".equals(role)) {
                return false;
            }

            // get the scope-mapped role, if present
            // else use the original role
            String resolvedRole = Optional.ofNullable(scope)
                .map(Scope::getRoleRefMap)
                .map(roleMap -> roleMap.get(role))
                .orElse(role);
            return securityContext.isUserInRole(resolvedRole);
        }
    }
}
