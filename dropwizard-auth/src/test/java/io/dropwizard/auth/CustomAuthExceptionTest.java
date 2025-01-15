package io.dropwizard.auth;

import io.dropwizard.auth.basic.BasicCredentialAuthFilter;
import io.dropwizard.auth.util.AuthUtil;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.logging.common.BootstrapLogging;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.ext.ExceptionMapper;
import org.glassfish.jersey.servlet.ServletProperties;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.jupiter.api.Test;

import java.security.Principal;
import java.util.Collections;

import static org.assertj.core.api.Assertions.assertThat;

class CustomAuthExceptionTest extends JerseyTest {
    static {
        BootstrapLogging.bootstrap();
    }

    static class CustomAuthException extends RuntimeException {
        public CustomAuthException(String message) {
            super(message);
        }
    }

    static class CustomUnauthorizedHandler implements UnauthorizedHandler {
        @Override
        public RuntimeException buildException(String prefix, String realm) {
            return new CustomAuthException("Authentication failed");
        }
    }

    static class CustomAuthExceptionMapper implements ExceptionMapper<CustomAuthException> {
        @Override
        public Response toResponse(CustomAuthException exception) {
            return Response.status(401).entity(exception.getMessage()).build();
        }
    }

    @Path("/custom")
    static class CustomAuthResource {
        @GET
        public String ping(@Auth Principal principal) {
            return "pong";
        }
    }

    static class CustomAuthResourceConfig extends DropwizardResourceConfig {
        public CustomAuthResourceConfig() {
            super();
            property(TestProperties.CONTAINER_PORT, "0");
            BasicCredentialAuthFilter.Builder<Principal> builder  = new BasicCredentialAuthFilter.Builder<>();
            builder.setAuthorizer(AuthUtil.getTestAuthorizer("admin", "admin"));
            builder.setAuthenticator(AuthUtil.getBasicAuthenticator(Collections.singletonList("admin")));
            builder.setPrefix("Custom");
            builder.setUnauthorizedHandler(new CustomUnauthorizedHandler());
            register(new AuthDynamicFeature(builder.buildAuthFilter()));
            register(new CustomAuthExceptionMapper());
            register(new CustomAuthResource());
        }
    }

    @Override
    protected TestContainerFactory getTestContainerFactory() throws TestContainerException {
        return new GrizzlyWebTestContainerFactory();
    }

    @Override
    protected DeploymentContext configureDeployment() {
        forceSet(TestProperties.CONTAINER_PORT, "0");
        return ServletDeploymentContext.builder(new CustomAuthResourceConfig())
            .initParam(ServletProperties.JAXRS_APPLICATION_CLASS, CustomAuthResourceConfig.class.getName())
            .build();
    }

    @Test
    void testCustomAuthException() {
        assertThat(target("/custom").request().get())
            .satisfies(response -> assertThat(response.getStatus()).isEqualTo(401))
            .satisfies(response -> assertThat(response.readEntity(String.class)).isEqualTo("Authentication failed"));
    }
}
