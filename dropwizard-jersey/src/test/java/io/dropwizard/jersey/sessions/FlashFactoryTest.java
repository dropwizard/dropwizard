package io.dropwizard.jersey.sessions;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.logging.BootstrapLogging;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletProperties;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.TestProperties;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.Test;

import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class FlashFactoryTest extends JerseyTest {
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
        final ResourceConfig rc = DropwizardResourceConfig.forTesting(new MetricRegistry());

        return ServletDeploymentContext.builder(rc)
                .initParam(ServletProperties.JAXRS_APPLICATION_CLASS, DropwizardResourceConfig.class.getName())
                .initParam(ServerProperties.PROVIDER_CLASSNAMES, FlashResource.class.getName())
                .build();
    }

    @Test
    public void passesInHttpSessions() throws Exception {
        Response firstResponse = target("/flash").request(MediaType.TEXT_PLAIN)
                .post(Entity.entity("Mr. Peeps", MediaType.TEXT_PLAIN));

        final Map<String, NewCookie> cookies = firstResponse.getCookies();
        firstResponse.close();

        Invocation.Builder builder = target("/flash").request().accept(MediaType.TEXT_PLAIN);

        for (NewCookie cookie : cookies.values()) {
            builder = builder.cookie(cookie);
        }

        final String secondResponse = builder.get(String.class);
        assertThat(secondResponse).isEqualTo("Mr. Peeps");

        Invocation.Builder anotherBuilder = target("/flash").request().accept(MediaType.TEXT_PLAIN);

        for (NewCookie cookie : cookies.values()) {
            anotherBuilder = anotherBuilder.cookie(cookie);
        }

        final String thirdResponse = anotherBuilder.get(String.class);
        assertThat(thirdResponse).isEqualTo("null");
    }
}

