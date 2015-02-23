package io.dropwizard.auth.basic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.failBecauseExceptionWasNotThrown;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.dropwizard.auth.Auth;
import io.dropwizard.auth.AuthFactory;
import io.dropwizard.auth.AuthResource;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.TransportObject;
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

import com.codahale.metrics.MetricRegistry;
import com.google.common.base.Optional;

public class AuthBeforeValidateTest extends JerseyTest {
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
        return ServletDeploymentContext.builder(new AuthBeforeValidateTestResourceConfig())
                .initParam(ServletProperties.JAXRS_APPLICATION_CLASS, AuthBeforeValidateTestResourceConfig.class.getName())
                .build();
    }
    
    @Test
    public void doNotLeakAnyValidationInfoOnFailedAuthentication() {
        try {
            TransportObject to = new TransportObject();
            Entity<TransportObject> entity = Entity.entity(to, MediaType.APPLICATION_JSON_TYPE);
            target("/greet").request()
                .header(HttpHeaders.AUTHORIZATION, "Basic Z29vZC1ndXk6c2VjcmV0")
                .post(entity, String.class);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException e) {
            Response r = e.getResponse();
            
            assertThat(r.getStatus()).isEqualTo(401);
            assertThat(r.getHeaders().get(HttpHeaders.WWW_AUTHENTICATE))
                    .containsOnly("Basic realm=\"realm\""); 
            assertThat(r.readEntity(String.class)).doesNotContain("may not be null");
        }
    }
    
    @Test
    public void doNotLeakAnyValidationInfoOnMissingAuthentication() {
        TransportObject to = new TransportObject();
        try {
            Entity<TransportObject> entity = Entity.entity(to, MediaType.APPLICATION_JSON_TYPE);
            // Note that when the AUTHORIZATION header is missing on a protected resource, 
            // we never even make it into the Authenticator instance, the client is presented
            // with a 401 immediately.
            target("/greet").request()
                .post(entity, String.class);
            failBecauseExceptionWasNotThrown(WebApplicationException.class);
        } catch (WebApplicationException e) {
            Response r = e.getResponse();
            
            assertThat(r.getStatus()).isEqualTo(401);
            assertThat(r.getHeaders().get(HttpHeaders.WWW_AUTHENTICATE))
                    .containsOnly("Basic realm=\"realm\""); 
            assertThat(r.readEntity(String.class)).doesNotContain("may not be null");
        }
    }
    
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/greet")
    public static class ValidatedResource {
        
        @POST
        public String greet(@Valid TransportObject validatedObject, @Auth String principal) {
            return "hello";
        }
    }
    
    public static class AuthBeforeValidateTestResourceConfig extends DropwizardResourceConfig {
        public AuthBeforeValidateTestResourceConfig() {
            super(true, new MetricRegistry());

            final Authenticator<BasicCredentials, String> denyAllAuthenticator = new Authenticator<BasicCredentials, String>() {
                @Override
                public Optional<String> authenticate(BasicCredentials credentials) throws AuthenticationException {
                    // Deny everyone access
                    return Optional.absent();
                }
            };
            register(AuthFactory.binder(new BasicAuthFactory<>(denyAllAuthenticator, "realm", String.class)));
            register(ValidatedResource.class);
        }
    }
}
