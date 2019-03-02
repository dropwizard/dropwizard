package com.example.helloworld.resources;

import com.example.helloworld.auth.ExampleAuthenticator;
import com.example.helloworld.auth.ExampleAuthorizer;
import com.example.helloworld.core.User;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.testing.junit5.DropwizardExtensionsSupport;
import io.dropwizard.testing.junit5.ResourceExtension;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import javax.ws.rs.ForbiddenException;
import javax.ws.rs.NotAuthorizedException;
import javax.ws.rs.core.HttpHeaders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

@ExtendWith(DropwizardExtensionsSupport.class)
public class ProtectedResourceTest {
    private static final BasicCredentialAuthFilter<User> BASIC_AUTH_HANDLER =
            new BasicCredentialAuthFilter.Builder<User>()
                    .setAuthenticator(new ExampleAuthenticator())
                    .setAuthorizer(new ExampleAuthorizer())
                    .setPrefix("Basic")
                    .setRealm("SUPER SECRET STUFF")
                    .buildAuthFilter();

    public static final ResourceExtension RULE = ResourceExtension.builder()
            .addProvider(RolesAllowedDynamicFeature.class)
            .addProvider(new AuthDynamicFeature(BASIC_AUTH_HANDLER))
            .addProvider(new AuthValueFactoryProvider.Binder<>(User.class))
            .setTestContainerFactory(new GrizzlyWebTestContainerFactory())
            .addProvider(ProtectedResource.class)
            .build();

    @Test
    public void testProtectedEndpoint() {
        String secret = RULE.target("/protected").request()
                .header(HttpHeaders.AUTHORIZATION, "Basic Z29vZC1ndXk6c2VjcmV0")
                .get(String.class);
        assertThat(secret).startsWith("Hey there, good-guy. You know the secret!");
    }

    @Test
    public void testProtectedEndpointNoCredentials401() {
        try {
            RULE.target("/protected").request()
                .get(String.class);
            failBecauseExceptionWasNotThrown(NotAuthorizedException.class);
        } catch (NotAuthorizedException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(401);
            assertThat(e.getResponse().getHeaders().get(HttpHeaders.WWW_AUTHENTICATE))
                    .containsOnly("Basic realm=\"SUPER SECRET STUFF\"");
        }

    }

    @Test
    public void testProtectedEndpointBadCredentials401() {
        try {
            RULE.target("/protected").request()
                .header(HttpHeaders.AUTHORIZATION, "Basic c25lYWt5LWJhc3RhcmQ6YXNkZg==")
                .get(String.class);
            failBecauseExceptionWasNotThrown(NotAuthorizedException.class);
        } catch (NotAuthorizedException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(401);
            assertThat(e.getResponse().getHeaders().get(HttpHeaders.WWW_AUTHENTICATE))
                .containsOnly("Basic realm=\"SUPER SECRET STUFF\"");
        }

    }

    @Test
    public void testProtectedAdminEndpoint() {
        String secret = RULE.target("/protected/admin").request()
                .header(HttpHeaders.AUTHORIZATION, "Basic Y2hpZWYtd2l6YXJkOnNlY3JldA==")
                .get(String.class);
        assertThat(secret).startsWith("Hey there, chief-wizard. It looks like you are an admin.");
    }

    @Test
    public void testProtectedAdminEndpointPrincipalIsNotAuthorized403() {
        try {
            RULE.target("/protected/admin").request()
                    .header(HttpHeaders.AUTHORIZATION, "Basic Z29vZC1ndXk6c2VjcmV0")
                    .get(String.class);
            failBecauseExceptionWasNotThrown(ForbiddenException.class);
        } catch (ForbiddenException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(403);
        }
    }
}
