package io.dropwizard.auth;

import static org.assertj.core.api.Assertions.assertThat;

import io.dropwizard.auth.principal.NullPrincipal;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.servlet.ServletProperties;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Test;

import java.io.IOException;
import java.security.Principal;

import javax.annotation.Priority;
import javax.ws.rs.Priorities;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

public class OptionalAuthFilterOrderingTest extends JerseyTest {

    @Override
    protected DeploymentContext configureDeployment() {
        forceSet(TestProperties.CONTAINER_PORT, "0");
        return ServletDeploymentContext
            .builder(new BasicAuthResourceConfigWithAuthorizationFilter())
            .initParam(ServletProperties.JAXRS_APPLICATION_CLASS,
                       BasicAuthResourceConfigWithAuthorizationFilter.class.getName())
            .build();
    }

    public static class BasicAuthResourceConfigWithAuthorizationFilter
        extends AbstractAuthResourceConfig {

        public BasicAuthResourceConfigWithAuthorizationFilter() {
            register(AuthResource.class);
            register(DummyAuthorizationFilter.class);
        }

        @Override
        protected Class<? extends Principal> getPrincipalClass() {
            return Principal.class;
        }

        @Override
        protected ContainerRequestFilter getAuthFilter() {
            return new DummyAuthenticationFilter();
        }

        @Override
        protected AbstractBinder getAuthBinder() {
            return new AuthValueFactoryProvider.Binder<>(getPrincipalClass());
        }

        @Override
        protected DynamicFeature getAuthDynamicFeature(ContainerRequestFilter authFilter) {
            return new AuthDynamicFeature(authFilter);
        }

    }

    @Test
    public void authenticationFilterShouldExecuteInAuthenticationPhaseForImplicitPermitall() {
        assertThat(target("/test/implicit-permitall").request().get(String.class))
            .isEqualTo("authorization ok");
    }

    @Test
    public void authenticationFilterShouldExecuteInAuthenticationPhaseForOptionalPrincipal() {
        assertThat(target("/test/optional").request().get(String.class))
            .isEqualTo("authorization ok");
    }

    @Priority(Priorities.AUTHENTICATION)
    private static class DummyAuthenticationFilter extends AuthFilter<Object, Principal> {

        @Override
        public void filter(ContainerRequestContext requestContext) throws IOException {
            requestContext.setSecurityContext(new SecurityContext() {
                @Override
                public Principal getUserPrincipal() {
                    return new NullPrincipal();
                }

                @Override
                public boolean isUserInRole(String s) {
                    return false;
                }

                @Override
                public boolean isSecure() {
                    return true;
                }

                @Override
                public String getAuthenticationScheme() {
                    return "doesn't matter";
                }
            });
        }
    }

    @Priority(Priorities.AUTHORIZATION)
    private static class DummyAuthorizationFilter implements ContainerRequestFilter {

        @Override
        public void filter(ContainerRequestContext request) throws IOException {
            if (request.getSecurityContext().getUserPrincipal() != null) {
                request.abortWith(Response.ok("authorization ok").build());
            } else {
                request.abortWith(Response.status(Response.Status.FORBIDDEN).build());
            }
        }
    }
}
