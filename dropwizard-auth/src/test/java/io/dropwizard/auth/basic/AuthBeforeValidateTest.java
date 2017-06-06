package io.dropwizard.auth.basic;

import static org.assertj.core.api.Assertions.assertThat;

import javax.validation.Valid;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import io.dropwizard.auth.Auth;
import io.dropwizard.auth.AuthFactory;
import io.dropwizard.auth.AuthenticationException;
import io.dropwizard.auth.Authenticator;
import io.dropwizard.auth.TransportObject;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.jersey.jackson.JsonProcessingExceptionMapper;
import io.dropwizard.jersey.validation.ConstraintViolationExceptionMapper;
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
    public void authenticatedRequestSucceeds() throws Exception {
        final TransportObject to = new TransportObject();
        to.setGivenName("abba");
        
        final Response response = target("/test").request()
            .header(HttpHeaders.AUTHORIZATION, "Basic Z29vZC1ndXk6c2VjcmV0")
            .post(Entity.entity(to, MediaType.APPLICATION_JSON_TYPE));
        
        assertThat(response.readEntity(String.class)).isEqualTo("abba");
    }
    
    @Test
    public void unauthenticatedRequestFailsWith401s() throws Exception {
        final TransportObject to = new TransportObject();
        to.setGivenName("abba");
        
        final Response response = target("/test").request()
            .post(Entity.entity(to, MediaType.APPLICATION_JSON_TYPE));
        
        assertThat(response.getStatus()).isEqualTo(401);
    }
    
    @Test
    public void unauthenticatedRequestWithOptionalAuthSucceeds() throws Exception {
        final TransportObject to = new TransportObject();
        to.setGivenName("abba");
        
        final Response response = target("/test/optional").request()
            .post(Entity.entity(to, MediaType.APPLICATION_JSON_TYPE));
        
        assertThat(response.readEntity(String.class)).isEqualTo("abba");
    }
    
    @Test
    public void authenticationMustHappenBeforeDeserialization() throws Exception {
        final Response response = target("/test").request()
            .post(Entity.entity("INVALID", MediaType.APPLICATION_JSON_TYPE));
        
        assertThat(response.getStatus()).isEqualTo(401);
    }
    
    @Test
    public void authenticationMustHappenBeforeValidation() throws Exception {
        final Response response = target("/test").request()
            .post(Entity.entity("{\"unknown\":123}", MediaType.APPLICATION_JSON_TYPE));
        
        assertThat(response.getStatus()).isEqualTo(401);
    }
    
    @Produces(MediaType.APPLICATION_JSON)
    @Consumes(MediaType.APPLICATION_JSON)
    @Path("/test")
    public static class ValidatedResource {
        
        @POST
        public String show(@Valid TransportObject validatedObject, @Auth String principal) {
            return validatedObject.getGivenName();
        }
        
        @POST
        @Path("/optional")
        public String optionalAuth(@Valid TransportObject validatedObject, @Auth(required = false) String principle) {
            return validatedObject.getGivenName();
        }
    }
    
  
    public static class BasicAuthenticator implements Authenticator<BasicCredentials, String> {

        @Override
        public Optional<String> authenticate(BasicCredentials credentials) throws AuthenticationException {
            if ("good-guy".equals(credentials.getUsername()) &&
                "secret".equals(credentials.getPassword())) {
                return Optional.of("good-guy");
            }
            return Optional.absent();
        }
        
    }
    
    public static class AuthBeforeValidateTestResourceConfig extends DropwizardResourceConfig {
        public AuthBeforeValidateTestResourceConfig() {
            super(true, new MetricRegistry());

            final Authenticator<BasicCredentials, String> basicAuthenticator = new BasicAuthenticator();
            register(AuthFactory.binder(new BasicAuthFactory<>(basicAuthenticator, "realm", String.class)));
            register(new ConstraintViolationExceptionMapper());
            register(new JsonProcessingExceptionMapper(true));
            register(ValidatedResource.class);
        }
    }
}
