package io.dropwizard.auth.principal;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import io.dropwizard.auth.*;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.logging.BootstrapLogging;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.servlet.ServletProperties;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.TestProperties;
import org.junit.Test;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.DynamicFeature;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.IOException;
import java.security.Principal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

/**
 * Testing that polymorphic principal entity injection works.
 */
public class PolymorphicPrincipalEntityTest extends JerseyTest {
    private static final String JSON_USERNAME = "good-guy";
    private static final String NULL_USERNAME = "bad-guy";
    private static final String JSON_USERNAME_ENCODED_TOKEN = "Z29vZC1ndXk6c2VjcmV0";
    private static final String NULL_USERNAME_ENCODED_TOKEN = "YmFkLWd1eTpzZWNyZXQ=";

    static {
        BootstrapLogging.bootstrap();
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
                ImmutableSet.of(JsonPrincipal.class, NullPrincipal.class));
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

            final BasicCredentialAuthFilter jsonAuthFilter = new BasicCredentialAuthFilter.Builder<JsonPrincipal>()
                .setAuthenticator(jsonAuthenticator)
                .buildAuthFilter();

            final BasicCredentialAuthFilter nullAuthFilter = new BasicCredentialAuthFilter.Builder<NullPrincipal>()
                .setAuthenticator(nullAuthenticator)
                .buildAuthFilter();

            return new PolymorphicAuthDynamicFeature<Principal>(ImmutableMap.of(
                JsonPrincipal.class, jsonAuthFilter,
                NullPrincipal.class, nullAuthFilter
            ));
        }
    }

    @Test
    public void jsonPrincipalEntityResourceAuth200() {
        assertThat(target("/auth-test/json-principal-entity").request()
                   .header(HttpHeaders.AUTHORIZATION, "Basic " + JSON_USERNAME_ENCODED_TOKEN)
                   .get(String.class))
                   .isEqualTo(JSON_USERNAME);
    }

    @Test
    public void jsonPrincipalEntityResourceNoAuth401() {
        try {
          target("/auth-test/json-principal-entity").request().get(String.class);
          failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(401);
        }
    }

    @Test
    public void nullPrincipalEntityResourceAuth200() {
        assertThat(target("/auth-test/null-principal-entity").request()
                .header(HttpHeaders.AUTHORIZATION, "Basic " + NULL_USERNAME_ENCODED_TOKEN)
                .get(String.class))
                .isEqualTo("null");
    }

    @Test
    public void nullPrincipalEntityResourceNoAuth401() {
        try {
          target("/auth-test/null-principal-entity").request().get(String.class);
          failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(401);
        }
    }
}
