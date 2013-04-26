package com.codahale.dropwizard.auth.basic;

import com.codahale.dropwizard.auth.Auth;
import com.codahale.dropwizard.auth.AuthenticationException;
import com.codahale.dropwizard.auth.Authenticator;
import com.codahale.dropwizard.jersey.DropwizardResourceConfig;
import com.codahale.dropwizard.logging.LoggingFactory;
import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Optional;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;


public class BasicAuthProviderTest extends JerseyTest {
    static {
        LoggingFactory.bootstrap();
    }

    @Path("/test/")
    @Produces(MediaType.TEXT_PLAIN)
    public static class ExampleResource {
        @GET
        public String show(@Auth String principal) {
            return principal;
        }
    }

    @Override
    protected AppDescriptor configure() {
        final DropwizardResourceConfig config = DropwizardResourceConfig.forTesting(new MetricRegistry());
        final Authenticator<BasicCredentials, String> authenticator = new Authenticator<BasicCredentials, String>() {
            @Override
            public Optional<String> authenticate(BasicCredentials credentials) throws AuthenticationException {
                if ("good-guy".equals(credentials.getUsername()) &&
                        "secret".equals(credentials.getPassword())) {
                    return Optional.of("good-guy");
                }
                if ("bad-guy".equals(credentials.getUsername())) {
                    throw new AuthenticationException("CRAP");
                }
                return Optional.absent();
            }
        };
        config.getSingletons().add(new BasicAuthProvider<>(authenticator, "realm"));
        config.getSingletons().add(new ExampleResource());
        return new LowLevelAppDescriptor.Builder(config).build();
    }

    @Test
    public void respondsToMissingCredentialsWith401() throws Exception {
        try {
            client().resource("/test").get(String.class);
            failBecauseExceptionWasNotThrown(UniformInterfaceException.class);
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus())
                    .isEqualTo(401);

            assertThat(e.getResponse().getHeaders().get(HttpHeaders.WWW_AUTHENTICATE))
                    .containsOnly("Basic realm=\"realm\"");
        }
    }

    @Test
    public void transformsCredentialsToPrincipals() throws Exception {
        assertThat(client().resource("/test")
                           .header(HttpHeaders.AUTHORIZATION, "Basic Z29vZC1ndXk6c2VjcmV0")
                           .get(String.class))
                .isEqualTo("good-guy");
    }

    @Test
    public void respondsToNonBasicCredentialsWith401() throws Exception {
        try {
            client().resource("/test")
                    .header(HttpHeaders.AUTHORIZATION, "Derp Z29vZC1ndXk6c2VjcmV0")
                    .get(String.class);
            failBecauseExceptionWasNotThrown(UniformInterfaceException.class);
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus())
                    .isEqualTo(401);

            assertThat(e.getResponse().getHeaders().get(HttpHeaders.WWW_AUTHENTICATE))
                    .containsOnly("Basic realm=\"realm\"");
        }
    }

    @Test
    public void respondsToExceptionsWith500() throws Exception {
        try {
            client().resource("/test")
                    .header(HttpHeaders.AUTHORIZATION, "Basic YmFkLWd1eTpzZWNyZXQ=")
                    .get(String.class);
            failBecauseExceptionWasNotThrown(UniformInterfaceException.class);
        } catch (UniformInterfaceException e) {
            assertThat(e.getResponse().getStatus())
                    .isEqualTo(500);
        }
    }
}
