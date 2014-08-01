package io.dropwizard.jersey.sessions;

import com.codahale.metrics.MetricRegistry;
import io.dropwizard.jersey.DropwizardResourceConfig;
import io.dropwizard.jersey.filter.AllowedMethodsFilter;
import io.dropwizard.jersey.filter.DummyResource;
import io.dropwizard.logging.LoggingFactory;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.core.Application;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;
import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;

import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;
import org.glassfish.jersey.servlet.ServletProperties;
import org.glassfish.jersey.test.DeploymentContext;
import org.glassfish.jersey.test.JerseyTest;
import org.glassfish.jersey.test.ServletDeploymentContext;
import org.glassfish.jersey.test.grizzly.GrizzlyWebTestContainerFactory;
import org.glassfish.jersey.test.spi.TestContainerException;
import org.glassfish.jersey.test.spi.TestContainerFactory;
import org.junit.Test;

public class HttpSessionFactoryTest extends JerseyTest {
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
        ResourceConfig rc = DropwizardResourceConfig.forTesting(new MetricRegistry());
        
        ServletDeploymentContext context = ServletDeploymentContext.builder(rc)
                .initParam(ServletProperties.JAXRS_APPLICATION_CLASS, 
                        DropwizardResourceConfig.class.getName())
                .initParam(ServerProperties.PROVIDER_CLASSNAMES, SessionResource.class.getName())
                .build();
  
        return context;
    }
    
    @Test
    public void passesInHttpSessions() throws Exception {
        Response firstResponse = target("/session/")
                .request(MediaType.TEXT_PLAIN)
                .post(Entity.entity(new String("Mr. Peeps"), MediaType.TEXT_PLAIN));

        final Map<String,NewCookie> cookies = firstResponse.getCookies();
        firstResponse.close();

        Invocation.Builder builder =
                target("/session/")
                .request()
                .accept(MediaType.TEXT_PLAIN);

        for (NewCookie cookie : cookies.values()) {
            builder.cookie(cookie);
        }

        final String secondResponse = builder.get(String.class);
        assertThat(secondResponse)
        .isEqualTo("Mr. Peeps");
    }
}
