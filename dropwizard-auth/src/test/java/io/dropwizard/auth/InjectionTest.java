package io.dropwizard.auth;

import org.checkerframework.checker.nullness.qual.Nullable;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.servlet.ServletProperties;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.TestProperties;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import javax.ws.rs.ForbiddenException;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.security.Principal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class InjectionTest extends JerseyTest {
    public static class TestInjectionAuthFilter extends AuthFilter<String, Principal> {
        @Inject
        @Nullable
        private InjectionManager injectionManager;

        public TestInjectionAuthFilter() {
            this.authenticator = credentials -> Optional.of(new PrincipalImpl("test"));
        }

        @Override
        public void filter(ContainerRequestContext requestContext) throws IOException {
            if (injectionManager == null) {
                throw new ForbiddenException();
            }
            if (!authenticate(requestContext, "test", "test")) {
                throw new WebApplicationException(Response.Status.UNAUTHORIZED);
            }
        }
    }

    public static class InjectionResourceConfig extends AbstractAuthResourceConfig {
        public InjectionResourceConfig() {
            register(AuthResource.class);
        }

        @Override
        protected ContainerRequestFilter getAuthFilter() {
            return new TestInjectionAuthFilter();
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
        Response response = target("/test/implicit-permitall").request().get();
        assertThat(response.getStatus()).isNotEqualTo(403).isEqualTo(200);
    }
}
