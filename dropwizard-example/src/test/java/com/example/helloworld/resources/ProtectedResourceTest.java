package com.example.helloworld.resources;

import com.example.helloworld.core.User;
import com.google.common.base.Optional;
import io.dropwizard.auth.AuthFactory;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.basic.BasicAuthFactory;
import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.testing.junit.ResourceTestRule;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.junit.ClassRule;
import org.junit.Test;

import javax.ws.rs.core.HttpHeaders;

import static org.assertj.core.api.Assertions.assertThat;

public class ProtectedResourceTest {

    final static Authenticator<BasicCredentials, User> authenticator = new Authenticator<BasicCredentials, User>() {
        @Override
        public Optional<User> authenticate(BasicCredentials credentials) throws AuthenticationException {
            if ("good-guy".equals(credentials.getUsername()) &&
                    "secret".equals(credentials.getPassword())) {
                return Optional.of(new User("good-guy"));
            }
            if ("bad-guy".equals(credentials.getUsername())) {
                throw new AuthenticationException("CRAP");
            }
            return Optional.absent();
        }
    };

    @ClassRule
    public static final ResourceTestRule resources = ResourceTestRule.builder()
            .addProvider(AuthFactory.binder(new BasicAuthFactory<>(authenticator, "realm", User.class)))
            .setTestContainerFactory(new GrizzlyWebTestContainerFactory())
            .addProvider(ProtectedResource.class)
            .build();

    @Test
    public void testProtectedEndpoint() {
        String secret = resources.getJerseyTest().target("/protected").request()
                .header(HttpHeaders.AUTHORIZATION, "Basic Z29vZC1ndXk6c2VjcmV0")
                .get(String.class);
        assertThat(secret).isEqualTo("Hey there, good-guy. You know the secret!");

    }
}
