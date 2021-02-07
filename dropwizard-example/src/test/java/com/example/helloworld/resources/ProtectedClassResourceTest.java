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
import javax.ws.rs.core.HttpHeaders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

@ExtendWith(DropwizardExtensionsSupport.class)
public final class ProtectedClassResourceTest {

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
        .addProvider(ProtectedClassResource.class)
        .build();

    @Test
    void testProtectedAdminEndpoint() {
        String secret = RULE.target("/protected/admin").request()
            .header(HttpHeaders.AUTHORIZATION, "Basic Y2hpZWYtd2l6YXJkOnNlY3JldA==")
            .get(String.class);
        assertThat(secret).startsWith("Hey there, chief-wizard. It looks like you are an admin.");
    }

    @Test
    void testProtectedBasicUserEndpoint() {
        String secret = RULE.target("/protected").request()
            .header(HttpHeaders.AUTHORIZATION, "Basic Z29vZC1ndXk6c2VjcmV0")
            .get(String.class);
        assertThat(secret).startsWith("Hey there, good-guy. You seem to be a basic user.");
    }

    @Test
    void testProtectedBasicUserEndpointAsAdmin() {
        String secret = RULE.target("/protected").request()
            .header(HttpHeaders.AUTHORIZATION, "Basic Y2hpZWYtd2l6YXJkOnNlY3JldA==")
            .get(String.class);
        assertThat(secret).startsWith("Hey there, chief-wizard. You seem to be a basic user.");
    }

    @Test
    void testProtectedGuestEndpoint() {
        String secret = RULE.target("/protected/guest").request()
            .header(HttpHeaders.AUTHORIZATION, "Basic Z3Vlc3Q6c2VjcmV0")
            .get(String.class);
        assertThat(secret).startsWith("Hey there, guest. You know the secret!");
    }

    @Test
    void testProtectedBasicUserEndpointPrincipalIsNotAuthorized403() {
        assertThatExceptionOfType(ForbiddenException.class)
            .isThrownBy(() -> RULE.target("/protected").request()
            .header(HttpHeaders.AUTHORIZATION, "Basic Z3Vlc3Q6c2VjcmV0")
            .get(String.class))
            .satisfies(e -> assertThat(e.getResponse().getStatus()).isEqualTo(403));
    }

}
