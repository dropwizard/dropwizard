package io.dropwizard.auth.oauth;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.ImmutableList;
import io.dropwizard.auth.AuthDynamicFeature;
import io.dropwizard.auth.AuthFilter;
import io.dropwizard.auth.AuthResource;
import io.dropwizard.auth.AuthValueFactoryProvider;
import io.dropwizard.auth.util.AuthUtil;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.logging.BootstrapLogging;
import org.glassfish.jersey.server.filter.RolesAllowedDynamicFeature;
import org.glassfish.jersey.servlet.ServletProperties;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.Test;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.HttpHeaders;

import java.security.Principal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

public class OAuthProviderTest extends JerseyTest {
    static {
        BootstrapLogging.bootstrap();
    }

    @Override
    protected TestContainerFactory getTestContainerFactory()
            throws TestContainerException {
        return new GrizzlyWebTestContainerFactory();
    }

    @Override
    protected DeploymentContext configureDeployment() {
        forceSet(TestProperties.CONTAINER_PORT, "0");
        return ServletDeploymentContext.builder(new BasicAuthTestResourceConfig())
                .initParam(ServletProperties.JAXRS_APPLICATION_CLASS, BasicAuthTestResourceConfig.class.getName())
                .build();
    }

    @Test
    public void respondsToMissingCredentialsWith401() throws Exception {
        try {
            target("/test/admin").request().get(String.class);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(401);
            assertThat(e.getResponse().getHeaders().get(HttpHeaders.WWW_AUTHENTICATE))
                    .containsOnly("Bearer realm=\"realm\"");
        }
    }

    @Test
    public void transformsCredentialsToPrincipals() throws Exception {
        assertThat(target("/test/admin").request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer good-guy")
                .get(String.class))
                .isEqualTo("'good-guy' has admin privileges");
    }

    @Test
    public void resourceWithAuthenticationWithoutAuthorizationWithCorrectCredentials200() {
        assertThat(target("/test/profile").request()
                .header(HttpHeaders.AUTHORIZATION, "Bearer ordinary-guy")
                .get(String.class))
                .isEqualTo("'ordinary-guy' has user privileges");
    }

    @Test
    public void resourceWithAuthenticationWithoutAuthorizationNoCredentials401() {
        try {
            target("/test/profile").request().get(String.class);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(401);
            assertThat(e.getResponse().getHeaders().get(HttpHeaders.WWW_AUTHENTICATE))
                    .containsOnly("Bearer realm=\"realm\"");
        }
    }

    @Test
    public void resourceWithAuthorizationPrincipalIsNotAuthorized403() {
        try {
            target("/test/admin").request()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer ordinary-guy")
                    .get(String.class);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(403);
        }
    }

    @Test
    public void resourceWithDenyAllAndNoAuth401() {
        try {
            target("/test/denied").request().get(String.class);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(401);
        }
    }

    @Test
    public void resourceWithDenyAllAndAuth403() {
        try {
            target("/test/denied").request()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer good-guy")
                    .get(String.class);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(403);
        }
    }

    @Test
    public void respondsToNonBasicCredentialsWith401() throws Exception {
        try {
            target("/test/admin").request().header(HttpHeaders.AUTHORIZATION, "Derp WHEE").get(String.class);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(401);
            assertThat(e.getResponse().getHeaders().get(HttpHeaders.WWW_AUTHENTICATE))
                    .containsOnly("Bearer realm=\"realm\"");
        }
    }

    @Test
    public void respondsToExceptionsWith500() throws Exception {
        try {
            target("/test/admin").request().header(HttpHeaders.AUTHORIZATION, "Bearer bad-guy").get(String.class);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException e) {
            assertThat(e.getResponse().getStatus()).isEqualTo(500);
        }
    }

    public static class BasicAuthTestResourceConfig extends DropwizardResourceConfig {
        public BasicAuthTestResourceConfig() {
            super(true, new MetricRegistry());

            register(new AuthValueFactoryProvider.Binder(Principal.class));
            register(new AuthDynamicFeature(getAuthFilter()));
            register(RolesAllowedDynamicFeature.class);
            register(AuthResource.class);
        }

        private AuthFilter getAuthFilter() {
            final String adminUser = "good-guy";
            final String ordinaryUser = "ordinary-guy";

            return new OAuthCredentialAuthFilter.Builder<>()
                    .setAuthenticator(AuthUtil.getMultiplyUsersOAuthAuthenticator(ImmutableList.of(adminUser, ordinaryUser)))
                    .setAuthorizer(AuthUtil.getTestAuthorizer(adminUser, "ADMIN"))
                    .setPrefix("Bearer")
                    .buildAuthFilter();
        }
    }
}
