package io.dropwizard.auth.basic;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.fest.assertions.api.Assertions.failBecauseExceptionWasNotThrown;
import io.dropwizard.auth.Auth;
import io.dropwizard.jersey.testing.JerseyServletTest;
import io.dropwizard.logging.LoggingFactory;

import java.util.Collections;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;
import org.junit.Test;

public class BasicAuthProviderTest extends JerseyServletTest {
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
    
    public BasicAuthProviderTest()
    {
        super("io.dropwizard.auth.basic.BasicAuthTestResourceConfig",
                Collections.<String>emptyList());
    }

    @Override
    protected Application configure() {
        return new BasicAuthTestResourceConfig();
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
    public void transformsCredentialsToPrincipals() throws Exception {
        assertThat(target("/test").request()
                .header(HttpHeaders.AUTHORIZATION, "Basic Z29vZC1ndXk6c2VjcmV0")
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
}
