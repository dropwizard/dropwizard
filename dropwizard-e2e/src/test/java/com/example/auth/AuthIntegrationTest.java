package com.example.auth;

import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.auth.Auth;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.PrincipalImpl;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.Base64;
import java.util.Objects;
import java.util.Optional;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.assertj.core.api.Assertions.assertThat;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(DropwizardExtensionsSupport.class)
public class AuthIntegrationTest {
    public static class TestApplication extends Application<Configuration> {
        public static void main(String[] args) throws Exception {
            new TestApplication().run(args);
        }

        @Override
        public void run(Configuration configuration, Environment environment) {
            environment.jersey().register(TestResource.class);
            AuthFilter<?, ?> basicAuthFilter = new BasicCredentialAuthFilter.Builder<PrincipalImpl>()
                .setAuthenticator(credentials -> Optional.of(new PrincipalImpl(credentials.getUsername())))
                .setAuthorizer((principal, role, requestContext) -> true)
                .buildAuthFilter();
            environment.jersey().register(new AuthDynamicFeature(basicAuthFilter));
            environment.jersey().register(new AuthValueFactoryProvider.Binder<>(PrincipalImpl.class));
            environment.healthChecks().register("dummy", new HealthCheck() {
                @Override
                protected Result check() {
                    return Result.healthy();
                }
            });
        }
    }

    @Path("/greet")
    public static class TestResource {
        @GET
        @Path("/authenticated")
        public String getAuthenticatedUser(@Auth PrincipalImpl principal,
                                           @Context HttpServletRequest httpServletRequest) {
            if (principal == null || httpServletRequest.getRemoteUser() == null
                || !Objects.equals(principal.getName(), httpServletRequest.getRemoteUser())) {
                throw new InternalServerErrorException("Expecting Jetty and Jersey principals to match");
            }
            return httpServletRequest.getRemoteUser();
        }
    }

    private final DropwizardAppExtension<Configuration> dropwizardAppRule = new DropwizardAppExtension<>(
        TestApplication.class,
        "auth/config.yml",
        new ResourceConfigurationSourceProvider());

    @Test
    void testRemoteUserIsSetCorrectly() {
        @SuppressWarnings("resource")
        final Client client = dropwizardAppRule.client();

        final String username = "admin";
        final String password = "";
        final String basicAuth = String.format("%s:%s", username, password);
        final String basicAuthHeader = "Basic " + Base64.getEncoder().encodeToString(basicAuth.getBytes(UTF_8));

        String url = String.format("http://localhost:%d/greet/authenticated", dropwizardAppRule.getLocalPort());
        String remoteUser = client.target(url).request()
            .header(HttpHeaders.AUTHORIZATION, basicAuthHeader)
            .get(String.class);

        assertThat(remoteUser).isEqualTo(username);
    }
}
