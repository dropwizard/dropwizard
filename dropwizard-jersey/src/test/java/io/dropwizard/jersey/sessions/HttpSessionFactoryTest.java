package io.dropwizard.jersey.sessions;

import io.dropwizard.jersey.AbstractJerseyTest;
import io.dropwizard.jersey.DropwizardResourceConfig;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.Invocation;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;
import jakarta.ws.rs.core.Response;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletProperties;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class HttpSessionFactoryTest extends AbstractJerseyTest {

    @Override
    protected TestContainerFactory getTestContainerFactory()
            throws TestContainerException {
        return new GrizzlyWebTestContainerFactory();
    }


    @Override
    protected DeploymentContext configureDeployment() {
        final ResourceConfig rc = DropwizardResourceConfig.forTesting();
        return ServletDeploymentContext.builder(rc)
                .initParam(ServletProperties.JAXRS_APPLICATION_CLASS, DropwizardResourceConfig.class.getName())
                .initParam(ServerProperties.PROVIDER_CLASSNAMES, SessionResource.class.getName())
                .build();
    }

    @Test
    void passesInHttpSessions() throws Exception {
        Response firstResponse = target("/session/").request(MediaType.TEXT_PLAIN)
                .post(Entity.entity("Mr. Peeps", MediaType.TEXT_PLAIN));

        final Map<String, NewCookie> cookies = firstResponse.getCookies();
        firstResponse.close();

        Invocation.Builder builder = target("/session/").request().accept(MediaType.TEXT_PLAIN);

        for (NewCookie cookie : cookies.values()) {
            builder.cookie(cookie);
        }

        assertThat(builder.get(String.class)).isEqualTo("Mr. Peeps");
    }
}
