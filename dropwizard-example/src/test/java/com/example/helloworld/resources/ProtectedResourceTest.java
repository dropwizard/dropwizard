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

import static org.assertj.core.api.Assertions.assertThat;

public class ProtectedResourceTest {
    private static final BasicCredentialAuthFilter<User> BASIC_AUTH_HANDLER;
    private static final Authenticator<BasicCredentials, User> AUTHENTICATOR;
    private static final Authorizer<User> AUTHORIZER;
    private static final String VALID_USER = "good-guy";
    private static final String VALID_ROLE = "ADMIN";

    static {
       AUTHENTICATOR = new Authenticator<BasicCredentials, User>() {
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
        AUTHORIZER = new Authorizer<User>() {
            @Override
            public boolean authorize(User user, String role) {
                return user != null
                        && VALID_USER.equals(user.getName())
                        && VALID_ROLE.equals(role);
            }
        };

        BASIC_AUTH_HANDLER = new BasicCredentialAuthFilter.Builder<User, Authenticator<BasicCredentials, User>>()
                .setAuthenticator(AUTHENTICATOR)
                .setAuthorizer(AUTHORIZER)
                .setPrefix("Basic")
                .buildAuthFilter();
    }

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addProvider(RolesAllowedDynamicFeature.class)
            .addProvider(new AuthDynamicFeature(BASIC_AUTH_HANDLER))
            .addProvider(new AuthValueFactoryProvider.Binder(User.class))
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
