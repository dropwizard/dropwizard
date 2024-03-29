package io.dropwizard.auth.principal;

import io.dropwizard.auth.AbstractAuthResourceConfig;
import io.dropwizard.auth.PolymorphicAuthDynamicFeature;
import io.dropwizard.auth.PolymorphicAuthValueFactoryProvider;
import io.dropwizard.logging.common.BootstrapLogging;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.MediaType;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.servlet.ServletProperties;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.TestProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.security.Principal;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testing that principal entity is not affected by authentication logic and can be injected as any other entity.
 */
class NoAuthPolymorphicPrincipalEntityTest extends JerseyTest {

    static {
        BootstrapLogging.bootstrap();
    }

    @Override
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
    }

    @Override
    @AfterEach
    public void tearDown() throws Exception {
        super.tearDown();
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
                Set.of(JsonPrincipal.class, NullPrincipal.class));
        }

        @Override protected DynamicFeature getAuthDynamicFeature(ContainerRequestFilter authFilter) {
            return new PolymorphicAuthDynamicFeature<>(Map.of(
                JsonPrincipal.class, getAuthFilter(),
                NullPrincipal.class, getAuthFilter()
            ));
        }
    }

    @Test
    void jsonPrincipalEntityResourceWithoutAuth200() {
        String principalName = "Astar Seran";
        assertThat(target("/no-auth-test/json-principal-entity").request()
                .header(HttpHeaders.AUTHORIZATION, "Anything here")
                .post(Entity.entity(new JsonPrincipal(principalName), MediaType.APPLICATION_JSON))
                .readEntity(String.class))
                .isEqualTo(principalName);
    }

    @Test
    void nullPrincipalEntityResourceWithoutAuth200() {
        assertThat(target("/no-auth-test/null-principal-entity").request()
                .header(HttpHeaders.AUTHORIZATION, "Anything here")
                .post(Entity.entity(new NullPrincipal(), MediaType.APPLICATION_JSON))
                .readEntity(String.class))
                .isEqualTo("null");
    }

    /**
     * When parameter is annotated then Jersey classifies such
     * parameter as {@link org.glassfish.jersey.server.model.Parameter.Source#UNKNOWN}
     * instead of {@link org.glassfish.jersey.server.model.Parameter.Source#ENTITY}
     * which is used for unannotated parameters. ValueFactoryProvider resolution
     * logic is different for these two sources therefore must be tested separately.
     */
    @Test
    void annotatedJsonPrincipalEntityResourceWithoutAuth200() {
        String principalName = "Astar Seran";
        assertThat(target("/no-auth-test/annotated-json-principal-entity").request()
                .header(HttpHeaders.AUTHORIZATION, "Anything here")
                .post(Entity.entity(new JsonPrincipal(principalName), MediaType.APPLICATION_JSON))
                .readEntity(String.class))
                .isEqualTo(principalName);
    }

    @Test
    void annotatedNullPrincipalEntityResourceWithoutAuth200() {
        assertThat(target("/no-auth-test/annotated-null-principal-entity").request()
                .header(HttpHeaders.AUTHORIZATION, "Anything here")
                .post(Entity.entity(new NullPrincipal(), MediaType.APPLICATION_JSON))
                .readEntity(String.class))
                .isEqualTo("null");
    }
}
