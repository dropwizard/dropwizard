package com.example.auth;

import com.codahale.metrics.health.HealthCheck;
import io.dropwizard.auth.Auth;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.PrincipalImpl;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.configuration.ResourceConfigurationSourceProvider;
import io.dropwizard.core.Application;
import io.dropwizard.core.Configuration;
import io.dropwizard.core.setup.Environment;
import io.dropwizard.testing.junit5.DropwizardAppExtension;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import jakarta.annotation.security.RolesAllowed;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.InternalServerErrorException;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.HttpHeaders;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.SecurityContext;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
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
    private static final String ADMIN_ROLE = "ADMIN";
    private static final BasicCredentials ADMIN_CREDENTIALS = new BasicCredentials("admin", "secret");
    private static final BasicCredentials USER_CREDENTIALS = new BasicCredentials("user", "secret");

    public static class TestApplication extends Application<Configuration> {
        public static void main(String[] args) throws Exception {
            new TestApplication().run(args);
        }

        @Override
        public void run(Configuration configuration, Environment environment) {
            environment.jersey().register(TestResource.class);
            AuthFilter<?, ?> basicAuthFilter = new BasicCredentialAuthFilter.Builder<PrincipalImpl>()
                .setAuthenticator(credentials -> {
                    if (ADMIN_CREDENTIALS.equals(credentials) || USER_CREDENTIALS.equals(credentials)) {
                        return Optional.of(new PrincipalImpl(credentials.getUsername()));
                    }
                    return Optional.empty();
                })
                .setAuthorizer((principal, role, requestContext) -> ADMIN_ROLE.equals(role)
                    && ADMIN_CREDENTIALS.getUsername().equals(principal.getName()))
                .buildAuthFilter();
            environment.jersey().register(new AuthDynamicFeature(basicAuthFilter));
            environment.jersey().register(new AuthValueFactoryProvider.Binder<>(PrincipalImpl.class));
            environment.jersey().register(RolesAllowedDynamicFeature.class);
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
                                           @Context HttpServletRequest httpServletRequest,
                                           @Context SecurityContext securityContext) {
            return describeIdentity(Optional.of(principal), httpServletRequest, securityContext);
        }

        @GET
        @Path("/admin")
        @RolesAllowed(ADMIN_ROLE)
        public String getAdminUser(@Auth PrincipalImpl principal,
                                   @Context HttpServletRequest httpServletRequest,
                                   @Context SecurityContext securityContext) {
            return describeIdentity(Optional.of(principal), httpServletRequest, securityContext);
        }

        @GET
        @Path("/optional")
        public String getOptionalUser(@Auth Optional<PrincipalImpl> principal,
                                      @Context HttpServletRequest httpServletRequest,
                                      @Context SecurityContext securityContext) {
            return describeIdentity(principal, httpServletRequest, securityContext);
        }

        private static String describeIdentity(Optional<PrincipalImpl> principal,
                                               HttpServletRequest httpServletRequest,
                                               SecurityContext securityContext) {
            if (!Objects.equals(principal.orElse(null), httpServletRequest.getUserPrincipal())
                || !Objects.equals(principal.orElse(null), securityContext.getUserPrincipal())
                || !Objects.equals(principal.map(PrincipalImpl::getName).orElse(null), httpServletRequest.getRemoteUser())
                || httpServletRequest.isUserInRole(ADMIN_ROLE) != securityContext.isUserInRole(ADMIN_ROLE)
                || (principal.isEmpty() && securityContext.isUserInRole(ADMIN_ROLE))) {
                throw new InternalServerErrorException("Expecting Jetty and Jersey principals and roles to match");
            }
            return principal.map(user -> user.getName() + ":" + httpServletRequest.isUserInRole(ADMIN_ROLE))
                .orElse("anonymous");
        }
    }

    private final DropwizardAppExtension<Configuration> dropwizardAppRule = new DropwizardAppExtension<>(
        TestApplication.class,
        "auth/config.yml",
        new ResourceConfigurationSourceProvider());

    @Test
    void propagatesAdminPrincipalAndRoles() {
        try (Response response = authenticatedRequest("authenticated", ADMIN_CREDENTIALS).get()) {
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.readEntity(String.class)).isEqualTo("admin:true");
        }
    }

    @Test
    void propagatesOrdinaryPrincipalAndRoles() {
        try (Response response = authenticatedRequest("authenticated", USER_CREDENTIALS).get()) {
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.readEntity(String.class)).isEqualTo("user:false");
        }
    }

    @Test
    void respondsToMissingCredentialsWith401() {
        try (Response response = request("authenticated").get()) {
            assertUnauthorized(response);
        }
    }

    @Test
    void respondsToWrongPasswordWith401() {
        try (Response response = authenticatedRequest("authenticated", new BasicCredentials("admin", "wrong")).get()) {
            assertUnauthorized(response);
        }
    }

    @Test
    void respondsToUnknownUserWith401() {
        try (Response response = authenticatedRequest("authenticated", new BasicCredentials("unknown", "secret")).get()) {
            assertUnauthorized(response);
        }
    }

    @Test
    void allowsAdminRole() {
        try (Response response = authenticatedRequest("admin", ADMIN_CREDENTIALS).get()) {
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.readEntity(String.class)).isEqualTo("admin:true");
        }
    }

    @Test
    void rejectsUserWithoutAdminRole() {
        try (Response response = authenticatedRequest("admin", USER_CREDENTIALS).get()) {
            assertThat(response.getStatus()).isEqualTo(403);
        }
    }

    @Test
    void challengesMissingCredentialsForAdminRole() {
        try (Response response = request("admin").get()) {
            assertUnauthorized(response);
        }
    }

    @Test
    void allowsOptionalAuthenticationWithoutCredentials() {
        try (Response response = request("optional").get()) {
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.readEntity(String.class)).isEqualTo("anonymous");
        }
    }

    @Test
    void allowsOptionalAuthenticationWithInvalidCredentials() {
        try (Response response = authenticatedRequest("optional", new BasicCredentials("admin", "wrong")).get()) {
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.readEntity(String.class)).isEqualTo("anonymous");
        }
    }

    @Test
    void propagatesOptionalPrincipal() {
        try (Response response = authenticatedRequest("optional", USER_CREDENTIALS).get()) {
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.readEntity(String.class)).isEqualTo("user:false");
        }
    }

    @Test
    void doesNotLeakAuthenticationBetweenRequests() {
        try (Response response = authenticatedRequest("optional", ADMIN_CREDENTIALS).get()) {
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.readEntity(String.class)).isEqualTo("admin:true");
        }
        try (Response response = authenticatedRequest("optional", USER_CREDENTIALS).get()) {
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.readEntity(String.class)).isEqualTo("user:false");
        }
        try (Response response = request("optional").get()) {
            assertThat(response.getStatus()).isEqualTo(200);
            assertThat(response.readEntity(String.class)).isEqualTo("anonymous");
        }
    }

    private Invocation.Builder request(String path) {
        @SuppressWarnings("resource")
        final Client client = dropwizardAppRule.client();
        final String url = String.format("http://localhost:%d/greet/%s", dropwizardAppRule.getLocalPort(), path);
        return client.target(url).request();
    }

    private Invocation.Builder authenticatedRequest(String path, BasicCredentials credentials) {
        final String basicAuth = credentials.getUsername() + ":" + credentials.getPassword();
        final String basicAuthHeader = "Basic " + Base64.getEncoder().encodeToString(basicAuth.getBytes(UTF_8));
        return request(path).header(HttpHeaders.AUTHORIZATION, basicAuthHeader);
    }

    private static void assertUnauthorized(Response response) {
        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getHeaderString(HttpHeaders.WWW_AUTHENTICATE)).isEqualTo("Basic realm=\"realm\"");
    }
}
