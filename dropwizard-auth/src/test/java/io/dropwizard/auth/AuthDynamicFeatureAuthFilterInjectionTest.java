package io.dropwizard.auth;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.jspecify.annotations.Nullable;
import org.glassfish.jersey.servlet.ServletProperties;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.TestProperties;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.security.Principal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class AuthDynamicFeatureAuthFilterInjectionTest extends JerseyTest {

    // helper Authenticator that retrieves the username from http header via injected HttpHeaders
    public static class InjectableAuthenticator implements Authenticator<String, Principal> {
        @Nullable
        @Context
        private HttpHeaders headers;

        @Override
        public Optional<Principal> authenticate(String credentials) throws AuthenticationException {
            if (headers != null) {
                String username = headers.getHeaderString("username");
                return Optional.of(() -> username);
            } else {
                throw new AuthenticationException("HttpHeaders not injected");
            }
        }
    }

    // helper Authorizer that determines if principal is allowed based on header value via injected HttpHeaders
    public static class InjectableAuthorizer implements Authorizer<Principal> {
        @Nullable
        @Context
        private HttpHeaders headers;

        @Override
        public boolean authorize(Principal principal, String role, @Nullable ContainerRequestContext requestContext) {
            if (headers != null) {
                String allowedRole = headers.getHeaderString("role");
                return allowedRole.equals(role);
            } else {
                throw new IllegalStateException("HttpHeaders not injected");
            }
        }
    }

    // helper auth filter that retrieves username from http header via injected HttpHeaders
    @Priority(Priorities.AUTHENTICATION)
    public static class TestInjectableAuthFilter extends AuthFilter<String, Principal> {

        public TestInjectableAuthFilter() {
            this.authenticator = new InjectableAuthenticator();
            this.authorizer = new InjectableAuthorizer();
        }

        @Override
        public void filter(ContainerRequestContext requestContext) throws IOException {
            if (!authenticate(requestContext, "ignored", SecurityContext.BASIC_AUTH)) {
                throw new WebApplicationException(Response.status(Response.Status.UNAUTHORIZED).build());
            }
        }
    }

    public static class InjectionResourceConfig extends AbstractAuthResourceConfig {
        public InjectionResourceConfig() {
            register(AuthResource.class);
        }

        @Override
        protected ContainerRequestFilter getAuthFilter() {
            return new TestInjectableAuthFilter();
        }
    }

    @Override
    protected DeploymentContext configureDeployment() {
        forceSet(TestProperties.CONTAINER_PORT, "0");
        return ServletDeploymentContext.builder(new InjectionResourceConfig())
            .initParam(ServletProperties.JAXRS_APPLICATION_CLASS, InjectionResourceConfig.class.getName())
            .build();
    }

    @Test
    void testInjectionSupport() {
        String req1User = "user1";
        String req2User = "user2";
        WebTarget target = target("/test/admin");

        String response1 = target.request()
            .header("username", req1User)
            .header("role", "ADMIN")
            .get(String.class);
        String response2 = target.request()
            .header("username", req2User)
            .header("role", "ADMIN")
            .get(String.class);
        Response response3 = target.request()
            .header("username", req1User)
            .header("role", "not-admin")
            .get();
        Response response4 = target.request()
            .header("username", req2User)
            .header("role", "not-admin")
            .get();

        assertThat(response1).isEqualTo("'%s' has admin privileges", req1User);
        assertThat(response2).isEqualTo("'%s' has admin privileges", req2User);
        assertThat(response3.getStatus()).isEqualTo(403);
        assertThat(response4.getStatus()).isEqualTo(403);
    }
}
