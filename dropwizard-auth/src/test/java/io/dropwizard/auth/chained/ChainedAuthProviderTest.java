package io.dropwizard.auth.chained;

import com.codahale.metrics.MetricRegistry;
import com.google.common.collect.Lists;
import io.dropwizard.auth.*;
import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.auth.basic.BasicCredentials;
import io.dropwizard.auth.oauth.OAuthCredentialAuthFilter;
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
import javax.annotation.security.RolesAllowed;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.SecurityContext;
import java.security.Principal;
import java.util.List;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

public class ChainedAuthProviderTest extends JerseyTest {
    private static final String ADMIN_ROLE = "ADMIN";
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
        @RolesAllowed({ADMIN_ROLE})
        @GET
        public String show(@Context SecurityContext context) {
            return context.getUserPrincipal().getName();
        }
    }

    public static class ChainedAuthTestResourceConfig extends DropwizardResourceConfig {
        public ChainedAuthTestResourceConfig() {
            super(true, new MetricRegistry());

            final String validUser = "good-guy";

            final Authorizer<Principal> authorizer =
                    AuthUtil.getTestAuthorizer(validUser, ADMIN_ROLE);

            final Authenticator<BasicCredentials, Principal> basicAuthenticator =
                    AuthUtil.getTestAuthenticatorBasicCredential(validUser);

            final Authenticator<String, Principal> oauthAuthenticator
                    = AuthUtil.getTestAuthenticator("A12B3C4D", validUser);

            final AuthFilter<BasicCredentials, Principal> basicAuthFilter =
                    new BasicCredentialAuthFilter.Builder<>()
                    .setAuthenticator(basicAuthenticator)
                    .setAuthorizer(authorizer)
                    .buildAuthFilter();

            final AuthFilter<String, Principal> oAuthFilter = new OAuthCredentialAuthFilter.Builder<>()
                    .setAuthenticator(oauthAuthenticator)
                    .setPrefix("Bearer")
                    .setAuthorizer(authorizer)
                    .buildAuthFilter();

            register(new AuthDynamicFeature(new ChainedAuthFilter<>(buildHandlerList(basicAuthFilter, oAuthFilter ))));
            register(RolesAllowedDynamicFeature.class);
            register(AuthResource.class);
        }

        @SuppressWarnings("unchecked")
        public List<AuthFilter> buildHandlerList(AuthFilter<BasicCredentials, Principal> basicAuthFilter,
                                                 AuthFilter<String, Principal> oAuthFilter) {
            final List<AuthFilter> handlers = Lists.newArrayList();
            handlers.add(basicAuthFilter);
            handlers.add(oAuthFilter);
            return handlers;
        }
    }
}
