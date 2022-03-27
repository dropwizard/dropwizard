package io.dropwizard.auth.principal;

import io.dropwizard.auth.AbstractAuthResourceConfig;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.PolymorphicAuthDynamicFeature;
import io.dropwizard.auth.PolymorphicAuthValueFactoryProvider;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.logging.common.BootstrapLogging;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.servlet.ServletProperties;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.TestProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import jakarta.ws.rs.WebApplicationException;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.container.ContainerRequestFilter;
import jakarta.ws.rs.container.DynamicFeature;
import jakarta.ws.rs.core.HttpHeaders;
import java.security.Principal;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

/**
 * Testing that polymorphic principal entity injection works.
 */
class PolymorphicPrincipalEntityTest extends JerseyTest {
    private static final String JSON_USERNAME = "good-guy";
    private static final String NULL_USERNAME = "bad-guy";
    private static final String JSON_USERNAME_ENCODED_TOKEN = "Z29vZC1ndXk6c2VjcmV0";
    private static final String NULL_USERNAME_ENCODED_TOKEN = "YmFkLWd1eTpzZWNyZXQ=";

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
                .builder(new PolymorphicPrincipalInjectedResourceConfig())
                .initParam(
                  ServletProperties.JAXRS_APPLICATION_CLASS,
                  PolymorphicPrincipalInjectedResourceConfig.class.getName())
                .build();
    }

    public static class PolymorphicPrincipalInjectedResourceConfig extends AbstractAuthResourceConfig {

        public PolymorphicPrincipalInjectedResourceConfig() {
            register(PolymorphicPrincipalEntityResource.class);
            packages("io.dropwizard.jersey.jackson");
        }

        @Override protected Class<? extends Principal> getPrincipalClass() {
            throw new AssertionError("getPrincipalClass must not be invoked");
        }

        @Override protected ContainerRequestFilter getAuthFilter() {
            return requestContext -> {
                throw new AssertionError("getAuthFilter result must not be invoked");
            };
        }

        @Override protected AbstractBinder getAuthBinder() {
            return new PolymorphicAuthValueFactoryProvider.Binder<>(
                Set.of(JsonPrincipal.class, NullPrincipal.class));
        }

        @Override protected DynamicFeature getAuthDynamicFeature(ContainerRequestFilter authFilter) {
            final Authenticator<BasicCredentials, JsonPrincipal> jsonAuthenticator = credentials -> {
                if (credentials.getUsername().equals(JSON_USERNAME)) {
                    return Optional.of(new JsonPrincipal(credentials.getUsername()));
                } else {
                    return Optional.empty();
                }
            };

            final Authenticator<BasicCredentials, NullPrincipal> nullAuthenticator = credentials -> {
                if (credentials.getUsername().equals(NULL_USERNAME)) {
                    return Optional.of(new NullPrincipal());
                } else {
                    return Optional.empty();
                }
            };

            final BasicCredentialAuthFilter<?> jsonAuthFilter = new BasicCredentialAuthFilter.Builder<JsonPrincipal>()
                .setAuthenticator(jsonAuthenticator)
                .buildAuthFilter();

            final BasicCredentialAuthFilter<?> nullAuthFilter = new BasicCredentialAuthFilter.Builder<NullPrincipal>()
                .setAuthenticator(nullAuthenticator)
                .buildAuthFilter();

            return new PolymorphicAuthDynamicFeature<>(Map.of(
                JsonPrincipal.class, jsonAuthFilter,
                NullPrincipal.class, nullAuthFilter
            ));
        }
    }

    @Test
    void jsonPrincipalEntityResourceAuth200() {
        assertThat(target("/auth-test/json-principal-entity").request()
                   .header(HttpHeaders.AUTHORIZATION, "Basic " + JSON_USERNAME_ENCODED_TOKEN)
                   .get(String.class))
                   .isEqualTo(JSON_USERNAME);
    }

    @Test
    void jsonPrincipalEntityResourceNoAuth401() {
        Invocation.Builder request = target("/auth-test/json-principal-entity").request();
        assertThatExceptionOfType(WebApplicationException.class)
            .isThrownBy(() -> request.get(String.class))
            .satisfies(e -> assertThat(e.getResponse().getStatus()).isEqualTo(401));
    }

    @Test
    void nullPrincipalEntityResourceAuth200() {
        assertThat(target("/auth-test/null-principal-entity").request()
                .header(HttpHeaders.AUTHORIZATION, "Basic " + NULL_USERNAME_ENCODED_TOKEN)
                .get(String.class))
                .isEqualTo("null");
    }

    @Test
    void nullPrincipalEntityResourceNoAuth401() {
        Invocation.Builder request = target("/auth-test/null-principal-entity").request();
        assertThatExceptionOfType(WebApplicationException.class)
            .isThrownBy(() -> request.get(String.class))
            .satisfies(e -> assertThat(e.getResponse().getStatus()).isEqualTo(401));
    }

    @Test
    void resourceWithValidOptionalAuthentication200() {
        assertThat(target("/auth-test/optional").request()
            .header(HttpHeaders.AUTHORIZATION, "Basic " + NULL_USERNAME_ENCODED_TOKEN)
            .get(String.class))
            .isEqualTo("principal was present");
    }

    @Test
    void resourceWithInvalidOptionalAuthentication200() {
        assertThat(target("/auth-test/optional").request()
            .header(HttpHeaders.AUTHORIZATION, "Basic cats")
            .get(String.class))
            .isEqualTo("principal was not present");
    }

    @Test
    void resourceWithoutOptionalAuthentication200() {
        assertThat(target("/auth-test/optional").request()
            .get(String.class))
            .isEqualTo("principal was not present");
    }

    @Test
    void resourceWithDifferentOptionalAuthentication200() {
        assertThat(target("/auth-test/optional").request()
            .header(HttpHeaders.AUTHORIZATION, "Basic " + JSON_USERNAME_ENCODED_TOKEN)
            .get(String.class))
            .isEqualTo("principal was not present");
    }
}
