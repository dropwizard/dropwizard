package io.dropwizard.auth;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Optional;
import com.sun.jersey.api.client.UniformInterfaceException;
import com.sun.jersey.test.framework.AppDescriptor;
import com.sun.jersey.test.framework.JerseyTest;
import com.sun.jersey.test.framework.LowLevelAppDescriptor;
import io.dropwizard.auth.basic.BasicAuthProvider;
import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.logging.LoggingFactory;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;

public class DelegatingAuthProviderTest  extends JerseyTest
{
    static {
        LoggingFactory.bootstrap();
    }

    @Path("/test/")
    @Produces(MediaType.TEXT_PLAIN)
    public static class ProtectedResource {
        @GET
        public String show(@Auth String principal) {
            return principal;
        }
    }

    @Override
    protected AppDescriptor configure() {
        final DropwizardResourceConfig config = DropwizardResourceConfig.forTesting(new MetricRegistry());
        final Authenticator<BasicCredentials, String> basicAuthenticator = new Authenticator<BasicCredentials, String>() {
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

        final Authenticator<String, String> oauthAuthenticator = new Authenticator<String, String>() {
            @Override
            public Optional<String> authenticate(String credentials) throws AuthenticationException {
                if ("A12B3C4D".equals(credentials)) {
                    return Optional.of("good-guy");
                }
                if ("bad-guy".equals(credentials)) {
                    throw new AuthenticationException("CRAP", new RuntimeException(""));
                }
                return Optional.absent();
            }
        };

        config.getSingletons().add(new DelegatingAuthProvider<>(basicAuthenticator, oauthAuthenticator, "realm"));
        config.getSingletons().add(new ProtectedResource());
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
                    .containsOnly("realm=\"realm\"");
        }
    }

    @Test
    public void transformsBasicCredentialsToPrincipals() throws Exception {
        assertThat(client().resource("/test")
                .header(HttpHeaders.AUTHORIZATION, "Basic Z29vZC1ndXk6c2VjcmV0")
                .get(String.class))
                .isEqualTo("good-guy");
    }

    @Test
    public void transformsBearerCredentialsToPrincipals() throws Exception {
        assertThat(client().resource("/test")
                .header(HttpHeaders.AUTHORIZATION, "Bearer A12B3C4D")
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
                    .containsOnly("realm=\"realm\"");
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
