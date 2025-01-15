package io.dropwizard.auth;

import jakarta.annotation.Priority;
import jakarta.ws.rs.Priorities;
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

import static org.assertj.core.api.Assertions.assertThat;

class AuthDynamicFeatureInjectionTest extends JerseyTest {

    // helper auth filter that retrieves username from http header via injected HttpHeaders
    @Priority(Priorities.AUTHENTICATION)
    public static class TestInjectableAuthenticationFilter implements ContainerRequestFilter {
        @Nullable
        @Context
        private HttpHeaders headers;

        @Override
        public void filter(ContainerRequestContext requestContext) throws IOException {
            if (headers != null) {
                String username = headers.getHeaderString("username");

                requestContext.setSecurityContext(new SecurityContext() {
                    @Override
                    public Principal getUserPrincipal() {
                        return () -> username;
                    }

                    @Override
                    public boolean isUserInRole(String role) {
                        return false;
                    }

                    @Override
                    public boolean isSecure() {
                        return false;
                    }

                    @Override
                    public String getAuthenticationScheme() {
                        return "";
                    }
                });
            } else {
                requestContext.abortWith(Response.serverError().entity("HttpHeaders not injected").build());
            }
        }
    }

    public static class InjectionResourceConfig extends AbstractAuthResourceConfig {
        public InjectionResourceConfig() {
            register(AuthResource.class);
        }

        @Override
        protected ContainerRequestFilter getAuthFilter() {
            return new TestInjectableAuthenticationFilter();
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
        WebTarget target = target("/test/implicit-permitall");

        String response1 = target.request()
            .header("username", req1User)
            .get(String.class);
        String response2 = target.request()
            .header("username", req2User)
            .get(String.class);

        assertThat(response1).isEqualTo("'%s' has user privileges", req1User);
        assertThat(response2).isEqualTo("'%s' has user privileges", req2User);
    }
}
