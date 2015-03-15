package com.example.helloworld.resources;

import com.example.helloworld.core.User;
import com.google.common.base.Function;
import com.google.common.base.Optional;
import io.dropwizard.auth.*;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.junit.ClassRule;
import org.junit.Test;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.SecurityContext;

import java.security.Principal;

import static org.assertj.core.api.Assertions.assertThat;

public class ProtectedResourceTest {
    private static BasicCredentialAuthFilter<User> BASIC_AUTH_HANDLER;
    static {
        final String VALID_USER = "good-guy";
        final String VALID_ROLE = "ADMIN";
        final Authenticator<BasicCredentials, User> AUTHENTICATOR =
                new Authenticator<BasicCredentials, User>() {
                    @Override
                    public Optional<User> authenticate(BasicCredentials credentials) throws AuthenticationException {
                        if (VALID_USER.equals(credentials.getUsername()) &&
                                "secret".equals(credentials.getPassword())) {
                            return Optional.of(new User(VALID_USER));
                        }
                        if ("bad-guy".equals(credentials.getUsername())) {
                            throw new AuthenticationException("CRAP");
                        }
                        return Optional.absent();
                    }
                };

        BasicCredentialAuthFilter.Builder<User, Authenticator<BasicCredentials, User>> builder
                = new BasicCredentialAuthFilter.Builder<>();
        builder.setSecurityContextFunction(new Function<AuthFilter.Tuple, SecurityContext>() {
            @Override
            public SecurityContext apply(final AuthFilter.Tuple input) {
                return new SecurityContext() {

                    @Override
                    public Principal getUserPrincipal() {
                        return input.getPrincipal();
                    }

                    @Override
                    public boolean isUserInRole(String role) {
                        return getUserPrincipal() != null
                                && VALID_USER.equals(getUserPrincipal().getName())
                                && VALID_ROLE.equals(role);
                    }

                    @Override
                    public boolean isSecure() {
                        return input.getContainerRequestContext().getSecurityContext().isSecure();
                    }

                    @Override
                    public String getAuthenticationScheme() {
                        return SecurityContext.BASIC_AUTH;
                    }
                };
            }
        });
        builder.setAuthenticator(AUTHENTICATOR);
        builder.setPrefix("Basic");
        BASIC_AUTH_HANDLER = builder.buildAuthHandler();
    }

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addProvider(RolesAllowedDynamicFeature.class)
            .addProvider(new AuthDynamicFeature(BASIC_AUTH_HANDLER))
            .setTestContainerFactory(new GrizzlyWebTestContainerFactory())
            .addProvider(ProtectedResource.class)
            .build();

    @Test
    public void testProtectedEndpoint() {
        String secret = resources.getJerseyTest().target("/protected").request()
                .header(HttpHeaders.AUTHORIZATION, "Basic Z29vZC1ndXk6c2VjcmV0")
                .get(String.class);
        assertThat(secret).startsWith("Hey there, good-guy. You know the secret!");
    }
}
