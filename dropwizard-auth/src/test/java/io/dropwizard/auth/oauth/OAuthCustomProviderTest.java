package io.dropwizard.auth.oauth;

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Optional;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthResource;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.PrincipalImpl;
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

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;

import java.security.Principal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

public class OAuthCustomProviderTest extends JerseyTest {
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
        return ServletDeploymentContext.builder(new BasicAuthTestResourceConfig())
                .initParam(ServletProperties.JAXRS_APPLICATION_CLASS, BasicAuthTestResourceConfig.class.getName())
                .build();
    }

    @Test
    public void respondsToMissingCredentialsWith401() throws Exception {
        try {
            target("/test").request().get(String.class);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(401);
            assertThat(e.getResponse().getHeaders().get(HttpHeaders.WWW_AUTHENTICATE))
                    .containsOnly("Custom realm=\"realm\"");
        }
    }

    @Test
    public void transformsCredentialsToPrincipals() throws Exception {
        assertThat(target("/test").request().header(HttpHeaders.AUTHORIZATION, "Custom good-guy").get(String.class))
                .isEqualTo("good-guy");
    }

    @Test
    public void resourceWithoutAuth200() {
        assertThat(target("/test/noauth").request()
                .get(String.class))
                .isEqualTo("hello");
    }

    @Test
    public void resourceWithAuthNotRequired200() {
        assertThat(target("/test/authnotrequired").request()
                .get(String.class))
                .isEqualTo("No Principal");
    }

    @Test
    public void respondsToNonBasicCredentialsWith401() throws Exception {
        try {
            target("/test").request().header(HttpHeaders.AUTHORIZATION, "Derp WHEE").get(String.class);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(401);
            assertThat(e.getResponse().getHeaders().get(HttpHeaders.WWW_AUTHENTICATE))
                    .containsOnly("Custom realm=\"realm\"");
        }
    }

    @Test
    public void respondsToExceptionsWith500() throws Exception {
        try {
            target("/test").request().header(HttpHeaders.AUTHORIZATION, "Custom bad-guy").get(String.class);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(500);
        }
    }

    public static class BasicAuthTestResourceConfig extends DropwizardResourceConfig {
        public BasicAuthTestResourceConfig() {
            super(true, new MetricRegistry());

            final Authenticator<String, Principal> authenticator = new Authenticator<String, Principal>() {
                @Override
                public Optional<Principal> authenticate(String credentials) throws AuthenticationException {
                    if ("good-guy".equals(credentials)) {
                        return Optional.<Principal>of(new PrincipalImpl("good-guy"));
                    }

                    if ("bad-guy".equals(credentials)) {
                        throw new AuthenticationException("CRAP");
                    }

                    return Optional.absent();
                }
            };

            register(new AuthDynamicFeature(
                    new OAuthCredentialAuthHandler.Builder<>()
                            .setAuthenticator(authenticator)
                            .setPrefix("Custom")
                            .buildAuthHandler()));

            register(AuthResource.class);
        }
    }
}
