package io.dropwizard.auth.principal;

import io.dropwizard.auth.AbstractAuthResourceConfig;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.logging.BootstrapLogging;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.servlet.ServletProperties;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.security.Principal;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testing that principal entity is not affected by authentication logic and can be injected as any other entity.
 */
public class NoAuthPrincipalEntityTest extends JerseyTest {

    static {
        BootstrapLogging.bootstrap();
    }

    @Override
    protected DeploymentContext configureDeployment() {
        forceSet(TestProperties.CONTAINER_PORT, "0");
        return ServletDeploymentContext
                .builder(new NoAuthPrincipalInjectedResourceConfig())
                .initParam(ServletProperties.JAXRS_APPLICATION_CLASS, NoAuthPrincipalInjectedResourceConfig.class.getName())
                .build();
    }

    public static class NoAuthPrincipalInjectedResourceConfig extends AbstractAuthResourceConfig {

        public NoAuthPrincipalInjectedResourceConfig() {
            register(NoAuthPrincipalEntityResource.class);
            packages("io.dropwizard.jersey.jackson");
        }

        @Override protected Class<? extends Principal> getPrincipalClass() {
            return JsonPrincipal.class;
        }

        @Override protected ContainerRequestFilter getAuthFilter() {
            return requestContext -> {
                throw new AssertionError("Authentication must not be performed");
            };
        }

        @Override protected AbstractBinder getAuthBinder() {
            return new AuthValueFactoryProvider.Binder<>(getPrincipalClass());
        }

        @Override protected DynamicFeature getAuthDynamicFeature(ContainerRequestFilter authFilter) {
            return new AuthDynamicFeature(authFilter);
        }
    }

    @Test
    public void principalEntityResourceWithoutAuth200() {
        String principalName = "Astar Seran";
        assertThat(target("/no-auth-test/principal-entity").request()
                .header(HttpHeaders.AUTHORIZATION, "Anything here")
                .post(Entity.entity(new JsonPrincipal(principalName), MediaType.APPLICATION_JSON))
                .readEntity(String.class))
                .isEqualTo(principalName);
    }

    /**
     * When parameter is annotated then Jersey classifies such parameter as {@link Parameter.Source#UNKNOWN} instead of
     * {@link Parameter.Source#ENTITY} which is used for unannotated parameters. ValueFactoryProvider resolution logic is different for these
     * two sources therefore must be tested separately.
     */
    @Test
    public void annotatedPrincipalEntityResourceWithoutAuth200() {
        String principalName = "Astar Seran";
        assertThat(target("/no-auth-test/annotated-principal-entity").request()
                .header(HttpHeaders.AUTHORIZATION, "Anything here")
                .post(Entity.entity(new JsonPrincipal(principalName), MediaType.APPLICATION_JSON))
                .readEntity(String.class))
                .isEqualTo(principalName);
    }
}
