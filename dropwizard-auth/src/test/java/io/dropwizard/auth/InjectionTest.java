package io.dropwizard.auth;

import jakarta.inject.Inject;
import jakarta.ws.rs.ForbiddenException;
import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.container.ContainerRequestContext;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.internal.inject.InjectionManager;
import org.glassfish.jersey.servlet.ServletProperties;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.TestProperties;
import org.junit.jupiter.api.Test;

import javax.annotation.Nullable;
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
