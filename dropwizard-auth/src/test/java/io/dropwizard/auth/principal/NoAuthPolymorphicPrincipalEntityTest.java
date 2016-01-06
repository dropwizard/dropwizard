package io.dropwizard.auth.principal;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.dropwizard.auth.*;
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
public class NoAuthPolymorphicPrincipalEntityTest extends JerseyTest {

    static {
        BootstrapLogging.bootstrap();
    }

    @Override
    protected DeploymentContext configureDeployment() {
        forceSet(TestProperties.CONTAINER_PORT, "0");
        return ServletDeploymentContext
                .builder(new NoAuthPolymorphicPrincipalInjectedResourceConfig())
                .initParam(
                  ServletProperties.JAXRS_APPLICATION_CLASS,
                  NoAuthPolymorphicPrincipalInjectedResourceConfig.class.getName())
                .build();
    }

    public static class NoAuthPolymorphicPrincipalInjectedResourceConfig extends AbstractAuthResourceConfig {

        public NoAuthPolymorphicPrincipalInjectedResourceConfig() {
            register(NoAuthPolymorphicPrincipalEntityResource.class);
            packages("io.dropwizard.jersey.jackson");
        }

        @Override protected Class<? extends Principal> getPrincipalClass() {
            throw new AssertionError("Authentication must not be performed");
        }

        @Override protected ContainerRequestFilter getAuthFilter() {
            return requestContext -> {
                throw new AssertionError("Authentication must not be performed");
            };
        }

        @Override protected AbstractBinder getAuthBinder() {
            return new PolymorphicAuthValueFactoryProvider.Binder<>(
                ImmutableSet.of(JsonPrincipal.class, NullPrincipal.class));
        }

        @Override protected DynamicFeature getAuthDynamicFeature(ContainerRequestFilter authFilter) {
            return new PolymorphicAuthDynamicFeature(ImmutableMap.of(
                JsonPrincipal.class, getAuthFilter(),
                NullPrincipal.class, getAuthFilter()
            ));
        }
    }

    @Test
    public void jsonPrincipalEntityResourceWithoutAuth200() {
        String principalName = "Astar Seran";
        assertThat(target("/no-auth-test/json-principal-entity").request()
                .header(HttpHeaders.AUTHORIZATION, "Anything here")
                .post(Entity.entity(new JsonPrincipal(principalName), MediaType.APPLICATION_JSON))
                .readEntity(String.class))
                .isEqualTo(principalName);
    }

    @Test
    public void nullPrincipalEntityResourceWithoutAuth200() {
        assertThat(target("/no-auth-test/null-principal-entity").request()
                .header(HttpHeaders.AUTHORIZATION, "Anything here")
                .post(Entity.entity(new NullPrincipal(), MediaType.APPLICATION_JSON))
                .readEntity(String.class))
                .isEqualTo("null");
    }

    /**
     * When parameter is annotated then Jersey classifies such
     * parameter as {@link Parameter.Source#UNKNOWN} instead of
     * {@link Parameter.Source#ENTITY} which is used for unannotated
     * parameters. ValueFactoryProvider resolution logic is different
     * for these two sources therefore must be tested separately.
     */
    @Test
    public void annotatedJsonPrincipalEntityResourceWithoutAuth200() {
        String principalName = "Astar Seran";
        assertThat(target("/no-auth-test/annotated-json-principal-entity").request()
                .header(HttpHeaders.AUTHORIZATION, "Anything here")
                .post(Entity.entity(new JsonPrincipal(principalName), MediaType.APPLICATION_JSON))
                .readEntity(String.class))
                .isEqualTo(principalName);
    }

    @Test
    public void annotatedNullPrincipalEntityResourceWithoutAuth200() {
        assertThat(target("/no-auth-test/annotated-null-principal-entity").request()
                .header(HttpHeaders.AUTHORIZATION, "Anything here")
                .post(Entity.entity(new NullPrincipal(), MediaType.APPLICATION_JSON))
                .readEntity(String.class))
                .isEqualTo("null");
    }
}
