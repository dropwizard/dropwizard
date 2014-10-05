package io.dropwizard.auth;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Optional;
import io.dropwizard.auth.basic.BasicAuthFactory;
import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.auth.oauth.OAuthFactory;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.logging.LoggingFactory;
import org.glassfish.jersey.servlet.ServletProperties;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.Test;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

public class ChainedAuthProviderTest extends JerseyTest {
    static {
        LoggingFactory.bootstrap();
    }

    @Override
    protected TestContainerFactory getTestContainerFactory()
            throws TestContainerException {
        return new GrizzlyWebTestContainerFactory();
    }

    @Override
    protected DeploymentContext configureDeployment() {
        return ServletDeploymentContext.builder(new ChainedAuthTestResourceConfig())
                .initParam(ServletProperties.JAXRS_APPLICATION_CLASS, ChainedAuthTestResourceConfig.class.getName())
                .build();
    }

    @Test
    public void respondsToMissingCredentialsWith401() throws Exception {
        try {
            target("/test").request().get(String.class);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException e) {
            assertThat(e.getResponse().getStatus())
                    .isEqualTo(401);

            assertThat(e.getResponse().getHeaders().get(HttpHeaders.WWW_AUTHENTICATE))
                    .containsOnly("Basic realm=\"realm\"");
        }
    }

    @Test
    public void transformsBasicCredentialsToPrincipals() throws Exception {
        assertThat(target("/test").request()
                .header(HttpHeaders.AUTHORIZATION, "Basic Z29vZC1ndXk6c2VjcmV0")
                .get(String.class))
                .isEqualTo("good-guy");
    }

    @Test
    public void transformsBearerCredentialsToPrincipals() throws Exception {
        assertThat(target("/test").request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer A12B3C4D")
                .get(String.class))
                .isEqualTo("good-guy");
    }


    @Test
    public void respondsToNonBasicCredentialsWith401() throws Exception {
        try {
            target("/test").request()
                    .header(HttpHeaders.AUTHORIZATION, "Derp Z29vZC1ndXk6c2VjcmV0")
                    .get(String.class);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException e) {
            assertThat(e.getResponse().getStatus())
                    .isEqualTo(401);

            assertThat(e.getResponse().getHeaders().get(HttpHeaders.WWW_AUTHENTICATE))
                    .containsOnly("Basic realm=\"realm\"");
        }
    }

    @Test
    public void respondsToExceptionsWith500() throws Exception {
        try {
            target("/test").request()
                    .header(HttpHeaders.AUTHORIZATION, "Basic YmFkLWd1eTpzZWNyZXQ=")
                    .get(String.class);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException e) {
            assertThat(e.getResponse().getStatus())
                    .isEqualTo(500);
        }
    }

    @Path("/test/")
    @Produces(MediaType.TEXT_PLAIN)
    public static class ProtectedResource {
        @GET
        public String show(@Auth String principal) {
            return principal;
        }
    }

    public static class ChainedAuthTestResourceConfig extends DropwizardResourceConfig {
        public ChainedAuthTestResourceConfig() {
            super(true, new MetricRegistry());

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
                        throw new AuthenticationException("CRAP");
                    }
                    return Optional.absent();
                }
            };

            BasicAuthFactory<String> basicAuthFactory = new BasicAuthFactory<String>(basicAuthenticator, "realm", String.class);
            OAuthFactory<String> oAuthFactory = new OAuthFactory<String>(oauthAuthenticator, "realm", String.class);

            register(AuthFactory.binder(new ChainedAuthFactory<String>(basicAuthFactory, oAuthFactory)));
            register(AuthResource.class);
        }
    }
}
